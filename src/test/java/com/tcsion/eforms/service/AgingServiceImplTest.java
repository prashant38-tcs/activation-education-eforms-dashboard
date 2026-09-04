package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.AgingThresholdConfig;
import com.tcsion.eforms.entity.StatusMaster;
import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.AgingThresholdConfigRepository;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.impl.AgingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** Critical test: aging calculation correctness. */
@ExtendWith(MockitoExtension.class)
class AgingServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private AgingThresholdConfigRepository agingThresholdConfigRepository;

    private AgingServiceImpl agingService;

    @BeforeEach
    void setUp() {
        agingService = new AgingServiceImpl(ticketRepository, agingThresholdConfigRepository);
        lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void agingDaysComputedFromAssignmentDateToToday() {
        Ticket ticket = Ticket.builder()
                .id(1L).ticketNumber("EF-1")
                .assignmentDate(LocalDateTime.now().minusDays(5))
                .currentStatus(StatusMaster.builder().statusCode(StatusMaster.WORK_IN_PROGRESS).open(true).build())
                .build();

        int aging = agingService.recalculateAging(ticket);
        assertEquals(5, aging);
        assertEquals(5, ticket.getAgingDays());
    }

    @Test
    void agingFreezesAtActualProductionDateForProductionTickets() {
        Ticket ticket = Ticket.builder()
                .id(2L).ticketNumber("EF-2")
                .assignmentDate(LocalDateTime.now().minusDays(20))
                .actualProductionDate(LocalDate.now().minusDays(10))
                .currentStatus(StatusMaster.builder().statusCode(StatusMaster.MOVED_TO_PRODUCTION).open(false).build())
                .build();

        int aging = agingService.recalculateAging(ticket);
        assertEquals(10, aging);
    }

    @Test
    void ticketWithoutAssignmentDateHasZeroAging() {
        Ticket ticket = Ticket.builder().id(3L).ticketNumber("EF-3").build();
        int aging = agingService.recalculateAging(ticket);
        assertEquals(0, aging);
    }

    @Test
    void bucketForClassifiesCorrectly() {
        when(agingThresholdConfigRepository.findByActiveTrueOrderByMinDaysAsc()).thenReturn(Arrays.asList(
                AgingThresholdConfig.builder().bucketCode("HEALTHY").minDays(0).maxDays(3).colorCode("success").label("Healthy").build(),
                AgingThresholdConfig.builder().bucketCode("ATTENTION").minDays(4).maxDays(7).colorCode("warning").label("Needs Attention").build(),
                AgingThresholdConfig.builder().bucketCode("AT_RISK").minDays(8).maxDays(15).colorCode("orange").label("At Risk").build(),
                AgingThresholdConfig.builder().bucketCode("CRITICAL").minDays(16).maxDays(null).colorCode("danger").label("Critical").build()
        ));

        assertEquals("HEALTHY", agingService.bucketFor(2));
        assertEquals("ATTENTION", agingService.bucketFor(5));
        assertEquals("AT_RISK", agingService.bucketFor(10));
        assertEquals("CRITICAL", agingService.bucketFor(30));
    }
}
