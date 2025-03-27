package com.example.repo.master;

import com.example.model.master.HolidayCalendar;
import com.example.model.master.SpecialTradingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Transactional
@Repository
public interface SpecialTradingSessionRepository extends JpaRepository<SpecialTradingSession, Long> {
    SpecialTradingSession findBySessionDate(LocalDate tradingDate);
}
