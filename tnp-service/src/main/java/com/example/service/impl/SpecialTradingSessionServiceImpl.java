package com.example.service.impl;

import com.example.data.transactional.entities.SpecialTradingSession;
import com.example.data.transactional.repo.SpecialTradingSessionRepository;
import com.example.service.SpecialTradingSessionService;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SpecialTradingSessionServiceImpl implements SpecialTradingSessionService {

    private final SpecialTradingSessionRepository specialTradingSessionRepository;

    @Override
    public Page<SpecialTradingSession> getAllSessions(
            int page, int size, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());

        if (from != null && to != null) {
            return specialTradingSessionRepository.findBySessionDateBetween(from, to, pageable);
        } else if (from != null) {
            return specialTradingSessionRepository.findBySessionDateAfter(from, pageable);
        } else if (to != null) {
            return specialTradingSessionRepository.findBySessionDateBefore(to, pageable);
        }
        return specialTradingSessionRepository.findAll(pageable);
    }

    @Override
    public Optional<SpecialTradingSession> getSessionByDate(LocalDate sessionDate) {
        return specialTradingSessionRepository.findBySessionDate(sessionDate);
    }

    @Override
    public SpecialTradingSession createSession(SpecialTradingSession session) {
        return specialTradingSessionRepository.save(session);
    }

    @Override
    public Optional<SpecialTradingSession> updateSession(Long id, SpecialTradingSession session) {
        return specialTradingSessionRepository
                .findById(id)
                .map(
                        existing -> {
                            existing.setSessionDate(session.getSessionDate());
                            existing.setStartTime(session.getStartTime());
                            existing.setEndTime(session.getEndTime());
                            existing.setDesc(session.getDesc());
                            return specialTradingSessionRepository.save(existing);
                        });
    }

    @Override
    public boolean deleteSession(Long id) {
        if (specialTradingSessionRepository.existsById(id)) {
            specialTradingSessionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
