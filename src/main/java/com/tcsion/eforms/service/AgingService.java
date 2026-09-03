package com.tcsion.eforms.service;

import com.tcsion.eforms.entity.Ticket;
import java.util.List;
import java.util.Map;

public interface AgingService {
    int recalculateAging(Ticket ticket);
    void recalculateAgingForAllOpenTickets();
    String bucketFor(int agingDays);
    Map<String, Long> getAgingBucketCounts();
    List<Ticket> getOldestOpenTickets(int limit);
    List<Ticket> getTicketsWithoutRecentUpdate(int days);
}
