package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RaidScheduler {

    private final RaidService raidService;

    RaidScheduler(RaidService raidService) {
        this.raidService = raidService;
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void activateDueRaids() {
        raidService.activateDueRaids();
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void expireActiveRaids() {
        raidService.expireActiveRaids();
    }

    @Scheduled(fixedDelay = 5 * 1000)
    public void expireOverdueTasks() {
        raidService.expireOverdueTasks();
    }
}
