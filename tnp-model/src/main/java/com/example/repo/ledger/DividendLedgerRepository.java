package com.example.repo.ledger;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.DividendLedger;

@Transactional
@Repository
public interface DividendLedgerRepository extends JpaRepository<DividendLedger, Long>{

}
