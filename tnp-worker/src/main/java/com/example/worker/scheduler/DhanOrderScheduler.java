package com.example.worker.scheduler;

import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.User;
import com.example.service.CalendarService;
import com.example.service.ResearchTechnicalService;
import com.example.service.UserService;
import com.example.service.dhan.DhanOrderExecutorService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DhanOrderScheduler {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final UserService userService;
    private final DhanOrderExecutorService dhanOrderExecutorService;
    private final ResearchTechnicalService<ResearchTechnical> researchTechnicalService;
    private final CalendarService calendarService;
    private final MiscUtil miscUtil;

    @Scheduled(cron = "0 15 9 * * *") // Runs at 9:00 AM daily
    public void processBuy() {
        log.info("Starting daily buy order processing at {}", LocalDateTime.now());
        try {
            LocalDate sessionDate = miscUtil.currentDate();
            if (calendarService.isWorkingDay(sessionDate)) {
                List<User> enabledUsers = userService.getAllDhanApiEnabledUsers();
                List<ResearchTechnical> researchTechnicals =
                        researchTechnicalService.getLatestBuyResearch(
                                calendarService.previousTradingSession(sessionDate));
                processOrdersInParallel(enabledUsers, researchTechnicals, OrderType.BUY);
            }
        } catch (Exception e) {
            log.error("Error in daily buy order processing", e);
        }
    }

    @Scheduled(cron = "0 15 9 * * *") // Runs at 9:00 AM daily
    public void processSell() {
        log.info("Starting daily sell order processing at {}", LocalDateTime.now());
        try {
            LocalDate sessionDate = miscUtil.currentDate();
            if (calendarService.isWorkingDay(sessionDate)) {
                List<User> enabledUsers = userService.getAllDhanApiEnabledUsers();
                List<ResearchTechnical> researchTechnicals =
                        researchTechnicalService.getLatestSellResearch(
                                calendarService.previousTradingSession(sessionDate));
                processOrdersInParallel(enabledUsers, researchTechnicals, OrderType.SELL);
            }
        } catch (Exception e) {
            log.error("Error in daily sell order processing", e);
        }
    }

    private void processOrdersInParallel(
            List<User> users, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        List<CompletableFuture<Void>> futures =
                users.stream()
                        .map(user -> createOrderFuture(user, researchTechnicals, orderType))
                        .collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("Completed processing {} orders for {} users", orderType, users.size());
    }

    private CompletableFuture<Void> createOrderFuture(
            User user, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        log.info(
                                "Processing {} orders for user: {}", orderType, user.getUsername());
                        executeOrder(user, researchTechnicals, orderType);
                    } catch (Exception e) {
                        log.error(
                                "Error processing {} orders for user: {}",
                                orderType,
                                user.getUsername(),
                                e);
                    }
                },
                executorService);
    }

    private void executeOrder(
            User user, List<ResearchTechnical> researchTechnicals, OrderType orderType) {
        switch (orderType) {
            case BUY:
                dhanOrderExecutorService.executeBuy(user, researchTechnicals);
                break;
            case SELL:
                dhanOrderExecutorService.executeSell(user, researchTechnicals);
                break;
        }
    }

    private enum OrderType {
        BUY,
        SELL
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down order executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
