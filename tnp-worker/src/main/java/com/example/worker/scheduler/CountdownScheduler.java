package com.example.worker.scheduler;

import java.util.Timer;
import java.util.TimerTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CountdownScheduler {

    private static final int START_TIME_SECONDS = 5 * 60; // 5 minutes

    private void startCountdown(String label) {
        Timer timer = new Timer();
        final int[] remainingSeconds = {START_TIME_SECONDS};

        TimerTask task =
                new TimerTask() {
                    @Override
                    public void run() {
                        if (remainingSeconds[0] <= 0) {
                            System.out.println(label + " countdown complete.");
                            timer.cancel();
                        } else {
                            int minutes = remainingSeconds[0] / 60;
                            int seconds = remainingSeconds[0] % 60;
                            System.out.printf(
                                    "[%s] Time left: %02d:%02d%n", label, minutes, seconds);
                            remainingSeconds[0]--;
                        }
                    }
                };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    // 🔔 Schedule 9:05 AM
    @Scheduled(cron = "0 0 9 * * MON-FRI") // Weekdays only
    public void countdownBeforeMorningSession() {
        startCountdown("Before 9:05 AM");
    }

    // 🔔 Schedule 3:20 PM
    @Scheduled(cron = "0 25 15 * * MON-FRI") // Weekdays only
    public void countdownBeforeEveningSession() {
        startCountdown("Before 3:30 PM");
    }
}
