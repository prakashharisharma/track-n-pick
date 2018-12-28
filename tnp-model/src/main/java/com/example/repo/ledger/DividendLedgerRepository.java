package com.example.repo.ledger;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.DividendLedger;
import com.example.model.um.User;

@Transactional
@Repository
public interface DividendLedgerRepository extends JpaRepository<DividendLedger, Long>{

	@Query(value = "SELECT sum(dl.perShareAmount * dl.quantity) from DividendLedger dl where dl.transactionDate BETWEEN :dateFrom AND :dateTo AND dl.userId = :userId")
	double getTotalDividendBetweenTwoDates(@Param("userId") User userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	
}
