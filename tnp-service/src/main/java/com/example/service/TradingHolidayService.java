package com.example.service;

import com.example.data.transactional.entities.TradingHoliday;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Optional;

public interface TradingHolidayService {
    Page<TradingHoliday> getAllHolidays(int page, int size, LocalDate from, LocalDate to);
    TradingHoliday createHoliday(TradingHoliday holiday);
    Optional<TradingHoliday> updateHoliday(Long id, TradingHoliday holiday);
    boolean deleteHoliday(Long id);
    Optional<TradingHoliday> getHolidayByDate(LocalDate date);
}
