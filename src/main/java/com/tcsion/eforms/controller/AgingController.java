package com.tcsion.eforms.controller;

import com.tcsion.eforms.entity.Ticket;
import com.tcsion.eforms.repository.TicketRepository;
import com.tcsion.eforms.service.AgingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/aging")
@RequiredArgsConstructor
public class AgingController {

    private final AgingService agingService;
    private final TicketRepository ticketRepository;

    @GetMapping
    public String agingDashboard(@RequestParam(required = false) String bucket, Model model) {
        model.addAttribute("bucketCounts", agingService.getAgingBucketCounts());
        model.addAttribute("oldestTickets", agingService.getOldestOpenTickets(15));
        model.addAttribute("noUpdateTickets", agingService.getTicketsWithoutRecentUpdate(3));

        List<Ticket> allOpen = ticketRepository.findAllOpenActive();
        if (bucket != null) {
            allOpen = allOpen.stream().filter(t -> bucket.equals(agingService.bucketFor(t.getAgingDays())))
                    .collect(Collectors.toList());
        }
        model.addAttribute("tickets", allOpen);
        model.addAttribute("selectedBucket", bucket);
        return "dashboard/aging-dashboard";
    }
}
