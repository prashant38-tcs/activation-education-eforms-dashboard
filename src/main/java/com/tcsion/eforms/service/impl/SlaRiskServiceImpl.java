package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.service.SlaRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transparent, rule-based SLA risk engine. Every score is accompanied by the
 * exact list of triggered factors so a user can see *why* a ticket is high
 * risk - this is explicitly NOT a machine-learning prediction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlaRiskServiceImpl implements SlaRiskService {

    private final TicketRepository ticketRepository;
    private final RiskWeightConfigRepository riskWeightConfigRepository;
    private final SlaThresholdConfigRepository slaThresholdConfigRepository;
    private final SlaRiskHistoryRepository slaRiskHistoryRepository;
    private final TicketActivityRepository ticketActivityRepository;

    private int configInt(String key, int defaultValue) {
        return slaThresholdConfigRepository.findByConfigKey(key)
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                }).orElse(defaultValue);
    }

    @Override
    @Transactional
    public SlaRiskHistory recalculateRisk(Ticket ticket) {
        Map<String, BigDecimal> weights = riskWeightConfigRepository.findByActiveTrue().stream()
                .collect(Collectors.toMap(RiskWeightConfig::getFactorCode, RiskWeightConfig::getWeight));
        BigDecimal totalWeight = weights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) totalWeight = BigDecimal.ONE;

        List<String> triggered = new ArrayList<>();
        BigDecimal earnedScore = BigDecimal.ZERO;

        int noActivityAlertDays = configInt("SLA_NO_ACTIVITY_ALERT_DAYS", 3);
        int approachingProductionDays = configInt("SLA_APPROACHING_PRODUCTION_DAYS", 2);
        int sameStatusAlertDays = configInt("SLA_SAME_STATUS_ALERT_DAYS", 5);

        if (ticket.getAgingDays() >= 15) {
            earnedScore = earnedScore.add(weights.getOrDefault("AGING", BigDecimal.ZERO));
            triggered.add("Ticket aging has exceeded 15 days (" + ticket.getAgingDays() + " days).");
        } else if (ticket.getAgingDays() >= 8) {
            earnedScore = earnedScore.add(weights.getOrDefault("AGING", BigDecimal.ZERO).multiply(new BigDecimal("0.6")));
            triggered.add("Ticket aging is at risk (" + ticket.getAgingDays() + " days).");
        }

        if (ticket.getPriority() != null && ticket.getPriority().getRankOrder() == 1) {
            earnedScore = earnedScore.add(weights.getOrDefault("PRIORITY", BigDecimal.ZERO));
            triggered.add("Ticket priority is Critical (P1).");
        } else if (ticket.getPriority() != null && ticket.getPriority().getRankOrder() == 2) {
            earnedScore = earnedScore.add(weights.getOrDefault("PRIORITY", BigDecimal.ZERO).multiply(new BigDecimal("0.5")));
        }

        if (ticket.getSeverity() != null && ticket.getSeverity().getRankOrder() == 1) {
            earnedScore = earnedScore.add(weights.getOrDefault("SEVERITY", BigDecimal.ZERO));
            triggered.add("Ticket severity is Sev-1 Blocker.");
        } else if (ticket.getSeverity() != null && ticket.getSeverity().getRankOrder() == 2) {
            earnedScore = earnedScore.add(weights.getOrDefault("SEVERITY", BigDecimal.ZERO).multiply(new BigDecimal("0.5")));
        }

        if (ticket.getEstimatedProductionDate() == null) {
            earnedScore = earnedScore.add(weights.getOrDefault("MISSING_ESTIMATED_DATE", BigDecimal.ZERO));
            triggered.add("Estimated Production Date is missing.");
        } else {
            long daysToProduction = ChronoUnit.DAYS.between(LocalDate.now(), ticket.getEstimatedProductionDate());
            if (daysToProduction < 0) {
                earnedScore = earnedScore.add(weights.getOrDefault("DAYS_TO_PRODUCTION", BigDecimal.ZERO));
                triggered.add("Ticket has exceeded its expected production date by " + Math.abs(daysToProduction) + " day(s).");
            } else if (daysToProduction <= approachingProductionDays) {
                earnedScore = earnedScore.add(weights.getOrDefault("DAYS_TO_PRODUCTION", BigDecimal.ZERO).multiply(new BigDecimal("0.7")));
                triggered.add("Expected production date is near (" + daysToProduction + " day(s) remaining).");
            }
        }

        if (ticket.getLastActivityDate() != null) {
            long daysSinceActivity = ChronoUnit.DAYS.between(ticket.getLastActivityDate().toLocalDate(), LocalDate.now());
            if (daysSinceActivity >= noActivityAlertDays) {
                earnedScore = earnedScore.add(weights.getOrDefault("DAYS_SINCE_ACTIVITY", BigDecimal.ZERO));
                triggered.add("No activity for " + daysSinceActivity + " day(s).");
            }
        } else {
            earnedScore = earnedScore.add(weights.getOrDefault("DAYS_SINCE_ACTIVITY", BigDecimal.ZERO));
            triggered.add("No activity has been recorded on this ticket yet.");
        }

        ticketActivityRepository.findFirstByTicket_IdOrderByActivityDatetimeDesc(ticket.getId()).ifPresent(lastActivity -> {
            long daysInStatus = ChronoUnit.DAYS.between(lastActivity.getActivityDatetime().toLocalDate(), LocalDate.now());
            if (daysInStatus >= sameStatusAlertDays) {
                triggered.add("Ticket has remained in the same status for more than " + sameStatusAlertDays + " days.");
            }
        });

        if (ticket.isOnHold()) {
            earnedScore = earnedScore.add(weights.getOrDefault("ON_HOLD", BigDecimal.ZERO));
            triggered.add("Ticket is currently On Hold.");
        }

        if (ticket.getCurrentStatus() != null
                && StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM.equals(ticket.getCurrentStatus().getStatusCode())) {
            earnedScore = earnedScore.add(weights.getOrDefault("FRAMEWORK_DEPENDENCY", BigDecimal.ZERO));
            triggered.add("Framework dependency is pending.");
        }
        if (ticket.getCurrentStatus() != null
                && StatusMaster.REASSIGNED_TO_OTHER_TEAM.equals(ticket.getCurrentStatus().getStatusCode())) {
            earnedScore = earnedScore.add(weights.getOrDefault("OTHER_TEAM_DEPENDENCY", BigDecimal.ZERO));
            triggered.add("Dependency on another team is pending.");
        }

        if (ticket.getReassignmentCount() >= 2) {
            earnedScore = earnedScore.add(weights.getOrDefault("REASSIGNMENT_COUNT", BigDecimal.ZERO));
            triggered.add("Ticket has been reassigned " + ticket.getReassignmentCount() + " times.");
        }

        if (ticket.getCurrentStatus() != null && StatusMaster.UAT_IN_PROGRESS.equals(ticket.getCurrentStatus().getStatusCode())) {
            earnedScore = earnedScore.add(weights.getOrDefault("UAT_PENDING", BigDecimal.ZERO));
            triggered.add("UAT is pending.");
        }
        if (ticket.getCurrentStatus() != null && StatusMaster.QA_IN_PROGRESS.equals(ticket.getCurrentStatus().getStatusCode())) {
            earnedScore = earnedScore.add(weights.getOrDefault("QA_PENDING", BigDecimal.ZERO));
            triggered.add("QA is pending.");
        }

        if ("BREACHED".equals(ticket.getSlaState())) {
            earnedScore = earnedScore.add(weights.getOrDefault("EXISTING_BREACH", BigDecimal.ZERO));
            triggered.add("Ticket already has an existing SLA breach.");
        }

        BigDecimal normalizedScore = earnedScore.multiply(new BigDecimal("100"))
                .divide(totalWeight, 2, RoundingMode.HALF_UP);
        normalizedScore = normalizedScore.min(new BigDecimal("100"));

        int lowMax = configInt("SLA_LOW_RISK_MAX", 40);
        int mediumMax = configInt("SLA_MEDIUM_RISK_MAX", 70);
        String category;
        if (normalizedScore.intValue() <= lowMax) category = "LOW";
        else if (normalizedScore.intValue() <= mediumMax) category = "MEDIUM";
        else category = "HIGH";

        String recommendedAction = buildRecommendedAction(category);

        ticket.setSlaRiskScore(normalizedScore);
        ticket.setSlaRiskCategory(category);
        if (ticket.getEstimatedProductionDate() != null && ticket.getEstimatedProductionDate().isBefore(LocalDate.now())
                && ticket.isOpen()) {
            ticket.setSlaState("BREACHED");
        } else if ("HIGH".equals(category)) {
            ticket.setSlaState("AT_RISK");
        } else {
            ticket.setSlaState("MET");
        }
        ticketRepository.save(ticket);

        SlaRiskHistory history = SlaRiskHistory.builder()
                .ticket(ticket).riskScore(normalizedScore).riskCategory(category)
                .calculationDate(LocalDateTime.now())
                .triggeredFactors(String.join(" | ", triggered))
                .recommendedAction(recommendedAction)
                .build();
        return slaRiskHistoryRepository.save(history);
    }

    private String buildRecommendedAction(String category) {
        if ("HIGH".equals(category)) {
            return "Escalate immediately: review blockers, confirm developer availability, and re-baseline the production date if required.";
        } else if ("MEDIUM".equals(category)) {
            return "Monitor closely and follow up with the assigned developer within 1-2 business days.";
        }
        return "No immediate action required; continue routine monitoring.";
    }

    @Override
    @Transactional
    public void recalculateRiskForAllOpenTickets() {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();
        for (Ticket ticket : openTickets) recalculateRisk(ticket);
        log.info("SLA risk recalculated for {} open tickets", openTickets.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getHighRiskTickets() {
        return ticketRepository.findByRiskCategory("HIGH");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> explainRisk(Ticket ticket) {
        return slaRiskHistoryRepository.findFirstByTicket_IdOrderByCalculationDateDesc(ticket.getId())
                .map(h -> Arrays.asList(h.getTriggeredFactors().split(" \\| ")))
                .orElse(Collections.emptyList());
    }
}
