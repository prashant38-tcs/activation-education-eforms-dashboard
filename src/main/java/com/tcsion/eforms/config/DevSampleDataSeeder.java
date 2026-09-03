package com.tcsion.eforms.config;

import com.tcsion.eforms.entity.*;
import com.tcsion.eforms.repository.*;
import com.tcsion.eforms.service.AgingService;
import com.tcsion.eforms.service.SlaRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Seeds realistic SYNTHETIC development-only data (Section 38): sample
 * developers, customers, and tickets spanning every status, plus activity
 * history, technical changes and deployments. Only runs with "dev" profile
 * AND app.sample-data.enabled=true. Every seeded ticket number is prefixed
 * "DEV-SAMPLE-" so it can never be confused with a real production ticket.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(20)
public class DevSampleDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMasterRepository customerMasterRepository;
    private final TicketTypeMasterRepository ticketTypeMasterRepository;
    private final PriorityMasterRepository priorityMasterRepository;
    private final SeverityMasterRepository severityMasterRepository;
    private final StatusMasterRepository statusMasterRepository;
    private final TeamMasterRepository teamMasterRepository;
    private final ActivityTypeMasterRepository activityTypeMasterRepository;
    private final TicketRepository ticketRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final TicketAssignmentRepository ticketAssignmentRepository;
    private final DeploymentDetailRepository deploymentDetailRepository;
    private final BackendChangeRepository backendChangeRepository;
    private final NotificationRepository notificationRepository;
    private final AgingService agingService;
    private final SlaRiskService slaRiskService;

    @Value("${app.sample-data.enabled:false}")
    private boolean sampleDataEnabled;

    private static final String SAMPLE_MARKER = "DEV-SAMPLE-";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!sampleDataEnabled) return;
        if (ticketRepository.existsByTicketNumberIgnoreCase(SAMPLE_MARKER + "0001")) {
            log.info("Synthetic development sample data already present - skipping DevSampleDataSeeder.");
            return;
        }
        log.info("Seeding SYNTHETIC development-only sample data (dev profile). None of this is real production data.");

        List<User> developers = seedSampleDevelopers();
        List<CustomerMaster> customers = seedSampleCustomers();
        seedSampleTickets(developers, customers);

        log.info("Synthetic sample data seeding complete: {} developers, {} customers, sample tickets across every status.",
                developers.size(), customers.size());
    }

    private List<User> seedSampleDevelopers() {
        Role developerRole = roleRepository.findByRoleCode(Role.DEVELOPER)
                .orElseThrow(() -> new IllegalStateException("DEVELOPER role not seeded."));
        String[][] devs = {
                {"swati.chandra", "Swati Chandra"}, {"vishal.salvi", "Vishal Salvi"},
                {"anuj.kumar", "Anuj Kumar"}, {"vaibhav.kaushik", "Vaibhav Kaushik"},
                {"pranjal.tayade", "Pranjal Tayade"}, {"rithik.p", "Rithik P"}
        };
        List<User> result = new ArrayList<>();
        for (String[] dev : devs) {
            if (userRepository.existsByUsernameIgnoreCase(dev[0])) {
                result.add(userRepository.findByUsernameIgnoreCase(dev[0]).get());
                continue;
            }
            Set<Role> roles = new HashSet<>();
            roles.add(developerRole);
            User user = User.builder()
                    .username(dev[0]).fullName(dev[1]).email(dev[0] + "@tcsion.example")
                    .passwordHash(passwordEncoder.encode(randomPassword()))
                    .active(true).forcePasswordChange(true).roles(roles)
                    .build();
            result.add(userRepository.save(user));
        }
        return result;
    }

    private List<CustomerMaster> seedSampleCustomers() {
        String[][] customers = {
                {"Sample State University", "Government"},
                {"Sample Private Institute of Technology", "Premium"},
                {"Sample Skill Development Board", "Standard"}
        };
        List<CustomerMaster> result = new ArrayList<>();
        for (String[] c : customers) {
            result.add(customerMasterRepository.findByCustomerNameIgnoreCase(c[0])
                    .orElseGet(() -> customerMasterRepository.save(CustomerMaster.builder()
                            .customerName(c[0]).customerCategory(c[1]).build())));
        }
        return result;
    }

    private void seedSampleTickets(List<User> developers, List<CustomerMaster> customers) {
        List<TicketTypeMaster> types = ticketTypeMasterRepository.findByActiveTrue();
        List<PriorityMaster> priorities = priorityMasterRepository.findByActiveTrueOrderByRankOrderAsc();
        List<SeverityMaster> severities = severityMasterRepository.findByActiveTrueOrderByRankOrderAsc();
        List<ActivityTypeMaster> activityTypes = activityTypeMasterRepository.findByActiveTrue();
        TeamMaster framework = teamMasterRepository.findByTeamCode("FRAMEWORK").orElse(null);
        TeamMaster sourceTeam = teamMasterRepository.findByTeamCode("AE_EFORMS").orElse(null);

        String[] statusCodes = {
                StatusMaster.NEW, StatusMaster.ASSIGNED, StatusMaster.WORK_IN_PROGRESS, StatusMaster.WORK_IN_PROGRESS,
                StatusMaster.UAT_IN_PROGRESS, StatusMaster.QA_IN_PROGRESS, StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM,
                StatusMaster.ON_HOLD, StatusMaster.MOVED_TO_PRODUCTION, StatusMaster.CLOSED
        };
        User systemUser = developers.get(0);

        for (int i = 0; i < statusCodes.length; i++) {
            String statusCode = statusCodes[i];
            StatusMaster status = statusMasterRepository.findByStatusCode(statusCode).orElseThrow();
            User assignedDeveloper = developers.get(i % developers.size());
            CustomerMaster customer = customers.get(i % customers.size());
            PriorityMaster priority = priorities.get(i % priorities.size());
            SeverityMaster severity = severities.get(i % severities.size());
            TicketTypeMaster type = types.get(i % types.size());

            int agingSeed = 1 + (i * 3);
            LocalDateTime assignmentDate = LocalDateTime.now().minusDays(agingSeed);

            Ticket ticket = Ticket.builder()
                    .ticketNumber(SAMPLE_MARKER + String.format("%04d", i + 1))
                    .crmId("CRM-SAMPLE-" + (1000 + i))
                    .customer(customer)
                    .ticketTitle("[Synthetic] Sample ticket #" + (i + 1) + " - " + status.getDisplayName())
                    .ticketDescription("This is SYNTHETIC development-only sample data used to demonstrate the "
                            + status.getDisplayName() + " workflow stage. It does not represent a real ticket.")
                    .shortPlannedMilestone("Sample milestone " + (i + 1))
                    .ticketType(type).priority(priority).severity(severity)
                    .assignedUser(assignedDeveloper).assignedBy(systemUser).assignmentDate(assignmentDate)
                    .sourceTeam(sourceTeam)
                    .dependencyTeam(StatusMaster.REASSIGNED_TO_FRAMEWORK_TEAM.equals(statusCode) ? framework : null)
                    .createdDate(assignmentDate.minusDays(1))
                    .expectedClosureDate(LocalDate.now().plusDays(10 - (i % 10)))
                    .estimatedProductionDate(LocalDate.now().plusDays(15 - (i % 12)))
                    .currentStatus(status)
                    .onHold(StatusMaster.ON_HOLD.equals(statusCode))
                    .holdReason(StatusMaster.ON_HOLD.equals(statusCode) ? "Awaiting customer clarification (sample)" : null)
                    .build();

            if (StatusMaster.MOVED_TO_PRODUCTION.equals(statusCode) || StatusMaster.CLOSED.equals(statusCode)) {
                ticket.setActualProductionDate(LocalDate.now().minusDays(2));
            }
            ticket = ticketRepository.save(ticket);

            ticketAssignmentRepository.save(TicketAssignment.builder()
                    .ticket(ticket).assignedToUser(assignedDeveloper).assignedBy(systemUser)
                    .assignmentType(TicketAssignment.INITIAL).reason("Synthetic sample data initial assignment")
                    .build());

            TicketActivity activity = ticketActivityRepository.save(TicketActivity.builder()
                    .ticket(ticket).previousStatus(status).newStatus(status)
                    .progressPercentage(40 + (i * 5) % 60)
                    .activityType(activityTypes.get(i % activityTypes.size()))
                    .workSummary("[Synthetic] Sample progress update for demonstration purposes.")
                    .detailedRemark("This activity record is part of the synthetic development dataset.")
                    .updatedBy(assignedDeveloper).source(TicketActivity.SOURCE_SYSTEM)
                    .build());

            ticket.setLastActivityDate(activity.getActivityDatetime());
            ticket.setLastUpdatedDate(activity.getActivityDatetime());
            ticketRepository.save(ticket);

            if (i % 3 == 0) {
                backendChangeRepository.save(BackendChange.builder()
                        .ticket(ticket).applicationModule("eforms-core")
                        .jarName("eforms-scoring-engine.jar").jarVersion("1.4." + i)
                        .buildNumber("BUILD-" + (2000 + i)).pipelineName("eforms-ci-pipeline")
                        .pipelineReference("https://ci.example.internal/job/eforms/" + (2000 + i))
                        .className("com.tcsion.eforms.sample.SampleProcessor")
                        .methodName("processSampleRequest")
                        .deploymentStatus(BackendChange.PENDING)
                        .createdBy(assignedDeveloper)
                        .changeDescription("[Synthetic] Sample backend change record.")
                        .build());
            }

            if (StatusMaster.MOVED_TO_PRODUCTION.equals(statusCode)) {
                deploymentDetailRepository.save(DeploymentDetail.builder()
                        .ticket(ticket).environment("PRODUCTION")
                        .jarName("eforms-scoring-engine.jar").jarVersion("1.4." + i)
                        .buildNumber("BUILD-" + (2000 + i)).pipelineName("eforms-ci-pipeline")
                        .deploymentDate(LocalDateTime.now().minusDays(2))
                        .deploymentOwner(assignedDeveloper)
                        .deploymentStatus(DeploymentDetail.COMPLETED)
                        .validationStatus("PASSED")
                        .deploymentRemark("[Synthetic] Sample production deployment record.")
                        .build());
            }

            notificationRepository.save(Notification.builder()
                    .recipient(assignedDeveloper)
                    .notificationType(Notification.NEW_TICKET_ASSIGNED)
                    .title("[Synthetic] Sample ticket assigned")
                    .message("Sample ticket " + ticket.getTicketNumber() + " assigned for demonstration purposes.")
                    .relatedTicket(ticket)
                    .build());

            agingService.recalculateAging(ticket);
            slaRiskService.recalculateRisk(ticket);
        }
    }

    private String randomPassword() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) sb.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        return sb.toString();
    }
}
