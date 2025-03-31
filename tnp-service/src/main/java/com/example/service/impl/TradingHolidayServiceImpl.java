package com.example.service.impl;

import com.example.data.transactional.entities.TradingHoliday;
import com.example.data.transactional.repo.TradingHolidayRepository;
import com.example.service.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TradingHolidayServiceImpl implements TradingHolidayService {

    private final TradingHolidayRepository tradingHolidayRepository;

    @Override
    public Page<TradingHoliday> getAllHolidays(int page, int size, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sessionDate").descending());
        if (from != null && to != null) {
            return tradingHolidayRepository.findBySessionDateBetween(from, to, pageable);
        }
        return tradingHolidayRepository.findAll(pageable);
    }

    @Override
    public TradingHoliday createHoliday(TradingHoliday holiday) {
        return tradingHolidayRepository.save(holiday);
    }

    @Override
    public Optional<TradingHoliday> updateHoliday(Long id, TradingHoliday holiday) {
        return tradingHolidayRepository.findById(id).map(existing -> {
            existing.setSessionDate(holiday.getSessionDate());
            existing.setDesc(holiday.getDesc());
            return tradingHolidayRepository.save(existing);
        });
    }

    @Override
    public boolean deleteHoliday(Long id) {
        if (tradingHolidayRepository.existsById(id)) {
            tradingHolidayRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TradingHoliday> getHolidayByDate(LocalDate date) {
        return tradingHolidayRepository.findBySessionDate(date);
    }
}