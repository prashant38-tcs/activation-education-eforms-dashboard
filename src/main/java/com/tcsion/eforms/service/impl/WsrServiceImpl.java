package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.entity.TicketActivity;
import com.tcsion.eforms.entity.WsrDailySnapshot;
import com.tcsion.eforms.repository.WsrDailySnapshotRepository;
import com.tcsion.eforms.service.WsrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WsrServiceImpl implements WsrService {

    private final WsrDailySnapshotRepository wsrRepository;

    @Override
    @Transactional
    public void recordFromActivity(TicketActivity activity) {
        if (wsrRepository.existsByActivity_Id(activity.getId())) return;
        Ticket ticket = activity.getTicket();
        WsrDailySnapshot snapshot = WsrDailySnapshot.builder()
                .snapshotDate(activity.getActivityDatetime().toLocalDate())
                .developer(activity.getUpdatedBy())
                .ticket(ticket)
                .activity(activity)
                .customer(ticket.getCustomer())
                .activityType(activity.getActivityType())
                .previousStatus(activity.getPreviousStatus())
                .currentStatus(activity.getNewStatus())
                .progressPercentage(activity.getProgressPercentage())
                .hoursSpent(activity.getHoursSpent())
                .remark(activity.getWorkSummary() != null ? activity.getWorkSummary() : activity.getDetailedRemark())
                .build();
        wsrRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WsrDailySnapshot> getDeveloperWsr(Long developerId, LocalDate start, LocalDate end) {
        return wsrRepository.findByDeveloper_IdAndSnapshotDateBetweenOrderBySnapshotDateDesc(developerId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WsrDailySnapshot> getTeamWsr(LocalDate start, LocalDate end) {
        return wsrRepository.findBySnapshotDateBetweenOrderBySnapshotDateDesc(start, end);
    }
}
