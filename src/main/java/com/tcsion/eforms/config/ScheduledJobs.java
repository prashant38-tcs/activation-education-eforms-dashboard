package com.tcsion.eforms.config;

import com.tcsion.eforms.service.AgingService;
import com.tcsion.eforms.service.SlaRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background recalculation so Aging / SLA Risk figures on every dashboard
 * are always current even if nobody happens to open the affected ticket
 * that day.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobs {

    private final AgingService agingService;
    private final SlaRiskService slaRiskService;

    @Scheduled(cron = "0 0 * * * *")
    public void recalculateAgingAndRisk() {
        log.info("Scheduled recalculation of ticket aging and SLA risk starting.");
        agingService.recalculateAgingForAllOpenTickets();
        slaRiskService.recalculateRiskForAllOpenTickets();
        log.info("Scheduled recalculation of ticket aging and SLA risk completed.");
    }
}
