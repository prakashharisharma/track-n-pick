package com.example.worker.scheduler;

import com.example.data.transactional.entities.User;
import com.example.service.BillingService;
import com.example.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateBillScheduler {

    private final UserService userService;
    private final BillingService billingService;

    // Runs at 9:00 AM on the 1st day of every month
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlyBills() {
        log.info("Starting monthly bill generation at {}", java.time.LocalDateTime.now());

        try {

            List<User> users = userService.getAllDhanApiEnabledUsers();

            users.forEach(
                    user -> {
                        try {
                            billingService.calculateAndRecordCharges(user);
                        } catch (Exception e) {
                            log.error(
                                    "Error processing bill for user {}: {}",
                                    user.getId(),
                                    e.getMessage(),
                                    e);
                        }
                    });

            log.info("Completed monthly bill generation for {} users", users.size());
        } catch (Exception e) {
            log.error("Error during monthly bill generation: {}", e.getMessage(), e);
        }
    }
}
