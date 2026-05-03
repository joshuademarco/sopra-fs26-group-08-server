package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HabitScheduler {

    private final HabitService habitService;

    HabitScheduler(HabitService habitService) {
        this.habitService = habitService;
    }

    // TODO: Isnt it enough to run this once a day? Smallest we have are daily habits
    // resets completed flag for new periods and breaks overdue streaks
    @Scheduled(fixedDelay = 60 * 1000) //runs every minute
    public void resetOverdueHabits() {
        habitService.resetOverdueHabits();
    }
}
