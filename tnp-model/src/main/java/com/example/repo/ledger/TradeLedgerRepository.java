package com.example.repo.ledger;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.TradeLedger;

@Transactional
@Repository
public interface TradeLedgerRepository extends JpaRepository<TradeLedger, Long> {

}
