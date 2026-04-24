package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RaidScheduler {

    private final RaidService raidService;

    RaidScheduler(RaidService raidService) {
        this.raidService = raidService;
    }

    //Every 60 seconds: activate raids whose scheduledTime is in past
    @Scheduled(fixedDelay = 60 * 1000)
    public void activateDueRaids() {
        raidService.activateDueRaids();
    }
}
