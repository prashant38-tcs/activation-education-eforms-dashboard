package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.TicketActivity;
import com.tcsion.eforms.entity.WsrDailySnapshot;
import java.time.LocalDate;
import java.util.List;

public interface WsrService {
    void recordFromActivity(TicketActivity activity);
    List<WsrDailySnapshot> getDeveloperWsr(Long developerId, LocalDate start, LocalDate end);
    List<WsrDailySnapshot> getTeamWsr(LocalDate start, LocalDate end);
}
