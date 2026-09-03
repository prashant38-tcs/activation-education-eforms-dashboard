package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.AgingThresholdConfig;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.AgingThresholdConfigRepository;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.AgingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgingServiceImpl implements AgingService {

    private final TicketRepository ticketRepository;
    private final AgingThresholdConfigRepository agingThresholdConfigRepository;

    @Override
    @Transactional
    public int recalculateAging(Ticket ticket) {
        if (ticket.getAssignmentDate() == null) {
            ticket.setAgingDays(0);
            return 0;
        }
        LocalDate assignmentDate = ticket.getAssignmentDate().toLocalDate();
        LocalDate referenceDate = ticket.getActualProductionDate() != null
                ? ticket.getActualProductionDate() : LocalDate.now();
        long days = ChronoUnit.DAYS.between(assignmentDate, referenceDate);
        int agingDays = (int) Math.max(0, days);
        ticket.setAgingDays(agingDays);
        ticketRepository.save(ticket);
        return agingDays;
    }

    @Override
    @Transactional
    public void recalculateAgingForAllOpenTickets() {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();
        for (Ticket ticket : openTickets) recalculateAging(ticket);
        log.info("Aging recalculated for {} open tickets", openTickets.size());
    }

    @Override
    @Transactional(readOnly = true)
    public String bucketFor(int agingDays) {
        List<AgingThresholdConfig> buckets = agingThresholdConfigRepository.findByActiveTrueOrderByMinDaysAsc();
        for (AgingThresholdConfig bucket : buckets) {
            boolean aboveMin = agingDays >= bucket.getMinDays();
            boolean belowMax = bucket.getMaxDays() == null || agingDays <= bucket.getMaxDays();
            if (aboveMin && belowMax) return bucket.getBucketCode();
        }
        return "CRITICAL";
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getAgingBucketCounts() {
        List<Ticket> openTickets = ticketRepository.findAllOpenActive();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("HEALTHY", 0L); counts.put("ATTENTION", 0L);
        counts.put("AT_RISK", 0L); counts.put("CRITICAL", 0L);
        for (Ticket t : openTickets) {
            String bucket = bucketFor(t.getAgingDays());
            counts.merge(bucket, 1L, Long::sum);
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getOldestOpenTickets(int limit) {
        return ticketRepository.findAllOpenActive().stream()
                .sorted(Comparator.comparingInt(Ticket::getAgingDays).reversed())
                .limit(limit).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsWithoutRecentUpdate(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return ticketRepository.findTicketsWithoutRecentUpdate(threshold);
    }
}
