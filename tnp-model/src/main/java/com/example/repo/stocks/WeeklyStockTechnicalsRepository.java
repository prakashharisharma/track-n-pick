package com.example.repo.stocks;

import com.example.model.stocks.WeeklyStockTechnicalsOld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface WeeklyStockTechnicalsRepository extends JpaRepository<WeeklyStockTechnicalsOld, Long> {
}
