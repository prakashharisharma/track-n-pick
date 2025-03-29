package com.example.transactional.repo.stocks;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.stocks.StockTechnicalsOld;

@Transactional
@Repository
public interface StockTechnicalsRepositoryOld extends JpaRepository<StockTechnicalsOld, Long> {

}
