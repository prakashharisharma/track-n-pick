package com.example.service;

import com.example.data.transactional.entities.NetWorthHistory;
import com.example.data.transactional.entities.User;
import com.example.data.transactional.repo.NetWorthHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetWorthHistoryService {

    private final PortfolioService portfolioService;
    private final NetWorthHistoryRepository netWorthHistoryRepository;

    /**
     * Records current net worth for a user
     *
     * @param user User to record net worth for
     * @return The recorded NetWorthHistory entry
     */
    @Transactional
    public NetWorthHistory recordNetWorth(User user) {
        return recordNetWorthForDate(user, LocalDate.now());
    }

    /**
     * Records net worth for a user on a specific date
     *
     * @param user User to record net worth for
     * @param date Date to record net worth for
     * @return The recorded NetWorthHistory entry
     */
    @Transactional
    public NetWorthHistory recordNetWorthForDate(User user, LocalDate date) {
        double netWorth = portfolioService.calculateNetWorth(user);
        double availableFund = portfolioService.availableFundLimit(user);
        double portfolioValue = netWorth - availableFund;

        // Check if entry already exists for this date
        Optional<NetWorthHistory> existing =
                netWorthHistoryRepository.findByUserAndDate(user, date);
        if (existing.isPresent()) {
            NetWorthHistory history = existing.get();
            history.setPortfolioValue(BigDecimal.valueOf(portfolioValue));
            history.setAvailableFund(BigDecimal.valueOf(availableFund));
            history.setNetWorth(BigDecimal.valueOf(netWorth));
            history.setUpdatedAt(LocalDateTime.now());
            return netWorthHistoryRepository.save(history);
        }

        // Create new entry
        NetWorthHistory history =
                NetWorthHistory.builder()
                        .user(user)
                        .date(date)
                        .portfolioValue(BigDecimal.valueOf(portfolioValue))
                        .availableFund(BigDecimal.valueOf(availableFund))
                        .netWorth(BigDecimal.valueOf(netWorth))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        NetWorthHistory saved = netWorthHistoryRepository.save(history);
        log.info("Recorded net worth for user {}: {} on date {}", user.getId(), netWorth, date);

        return saved;
    }

    /**
     * Gets latest recorded net worth for a user
     *
     * @param user User to get net worth for
     * @return Optional containing latest NetWorthHistory if exists
     */
    @Transactional(readOnly = true)
    public Optional<NetWorthHistory> getLatestNetWorth(User user) {
        return netWorthHistoryRepository.findFirstByUserOrderByDateDesc(user);
    }

    /**
     * Gets net worth history for a user between dates
     *
     * @param user User to get history for
     * @param startDate Start date inclusive
     * @param endDate End date inclusive
     * @return List of NetWorthHistory entries ordered by date descending
     */
    @Transactional(readOnly = true)
    public List<NetWorthHistory> getNetWorthHistory(
            User user, LocalDate startDate, LocalDate endDate) {
        return netWorthHistoryRepository.findByUserAndDateBetweenOrderByDateDesc(
                user, startDate, endDate);
    }
}
