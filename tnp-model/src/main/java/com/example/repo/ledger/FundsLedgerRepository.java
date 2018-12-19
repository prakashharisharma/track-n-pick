package com.example.repo.ledger;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.FundsLedger;

@Transactional
@Repository
public interface FundsLedgerRepository extends JpaRepository<FundsLedger, Long>{

}
