package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.PerformanceLedger;
import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface PerformanceLedgerRepository extends JpaRepository<PerformanceLedger, Long> {
	List<PerformanceLedger> findByUserIdAndPerformanceDateGreaterThanEqual(UserProfile uerId,LocalDate performanceDate);
}
