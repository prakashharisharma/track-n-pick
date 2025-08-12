package com.example.worker.scheduler;

import com.example.data.transactional.entities.User;
import com.example.service.NetWorthHistoryService;
import com.example.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordNetWorthScheduler {

    private final UserService userService;
    private final NetWorthHistoryService netWorthHistoryService;

    /**
     * Records net worth for all users with Dhan API enabled. Runs at 8:45 AM on the 1st day of
     * every month
     */
    @Scheduled(cron = "0 45 8 1 * ?")
    public void recordMonthlyNetWorth() {
        log.info("Starting monthly net worth recording at {}", LocalDateTime.now());

        try {
            List<User> users = userService.getAllDhanApiEnabledUsers();

            users.forEach(
                    user -> {
                        try {
                            netWorthHistoryService.recordNetWorth(user);
                        } catch (Exception e) {
                            log.error(
                                    "Error recording net worth for user {}: {}",
                                    user.getId(),
                                    e.getMessage(),
                                    e);
                        }
                    });

            log.info("Completed monthly net worth recording for {} users", users.size());
        } catch (Exception e) {
            log.error("Error during monthly net worth recording: {}", e.getMessage(), e);
        }
    }
}
