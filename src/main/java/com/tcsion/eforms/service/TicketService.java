package com.tcsion.eforms.service;

import com.tcsion.eforms.dto.request.*;
import com.tcsion.eforms.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TicketService {
    Ticket createTicket(TicketCreateRequest request);
    Ticket updateTicket(Long ticketId, TicketUpdateRequest request);
    Ticket getTicketForView(Long ticketId);
    Page<Ticket> getMyTickets(Long developerId, Pageable pageable);
    Page<Ticket> getAllTickets(String search, Long statusId, Long priorityId, Long customerId,
                                Long developerId, String riskCategory, Pageable pageable);
    List<Ticket> getMyOpenTickets(Long developerId);
    Ticket changeStatus(Long ticketId, TicketStatusChangeRequest request);
    Ticket reassignTicket(Long ticketId, TicketReassignRequest request);
    Ticket addActivity(Long ticketId, TicketActivityRequest request);
    void addComment(Long ticketId, CommentRequest request);
    void addFrontendChange(Long ticketId, FrontendChangeRequest request);
    void addBackendChange(Long ticketId, BackendChangeRequest request);
    void addDatabaseChange(Long ticketId, DatabaseChangeRequest request);
    boolean isTicketNumberAvailable(String ticketNumber);
}
