package com.example.repo.ledger;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.PerformanceLedger;

@Transactional
@Repository
public interface PerformanceLedgerRepository extends JpaRepository<PerformanceLedger, Long> {

}
