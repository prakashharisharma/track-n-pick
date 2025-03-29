package com.example.transactional.repo.stocks;

import com.example.transactional.model.stocks.WeeklyStockTechnicalsOld;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface WeeklyStockTechnicalsRepository extends JpaRepository<WeeklyStockTechnicalsOld, Long> {
}
