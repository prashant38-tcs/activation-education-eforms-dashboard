package com.tcsion.eforms.service;

import com.tcsion.eforms.dto.request.TicketCreateRequest;
import com.tcsion.eforms.exception.DuplicateResourceException;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Critical test: "Duplicate ticket cannot be created". */
@ExtendWith(MockitoExtension.class)
class TicketServiceDuplicateTest {

    @Mock private TicketRepository ticketRepository;

    @Test
    void createTicketRejectsDuplicateTicketNumber() {
        when(ticketRepository.existsByTicketNumberIgnoreCase("EF-DUP-0001")).thenReturn(true);

        // TicketServiceImpl has 22 constructor args; only ticketRepository is
        // exercised before the duplicate check throws, so the rest are safely null.
        TicketServiceImpl service = new TicketServiceImpl(
                ticketRepository,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);

        TicketCreateRequest request = new TicketCreateRequest();
        request.setTicketNumber("EF-DUP-0001");
        request.setCustomerId(1L);
        request.setTicketTitle("Duplicate check");
        request.setTicketTypeId(1L);
        request.setPriorityId(1L);
        request.setSeverityId(1L);

        assertThrows(DuplicateResourceException.class, () -> service.createTicket(request));
    }
}
