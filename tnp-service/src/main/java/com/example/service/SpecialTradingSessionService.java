package com.example.service;

import com.example.data.transactional.entities.SpecialTradingSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface SpecialTradingSessionService {

    Page<SpecialTradingSession> getAllSessions(int page, int size, LocalDate from, LocalDate to);

    Optional<SpecialTradingSession> getSessionByDate(LocalDate sessionDate);

    SpecialTradingSession createSession(SpecialTradingSession session);

    Optional<SpecialTradingSession> updateSession(Long id, SpecialTradingSession session);

    boolean deleteSession(Long id);

    public List<SpecialTradingSession> getUpcomingSpecialSessions();
}
