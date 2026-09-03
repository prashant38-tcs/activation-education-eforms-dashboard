package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.SlaRiskHistory;
import com.tcsion.eforms.entity.Ticket;
import java.util.List;

public interface SlaRiskService {
    SlaRiskHistory recalculateRisk(Ticket ticket);
    void recalculateRiskForAllOpenTickets();
    List<Ticket> getHighRiskTickets();
    List<String> explainRisk(Ticket ticket);
}
