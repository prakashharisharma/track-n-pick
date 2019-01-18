package com.example.repo.ledger;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.TradeLedger;
import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface TradeLedgerRepository extends JpaRepository<TradeLedger, Long> {

	@Query(value = "SELECT sum(tl.securityTxnTax + stampDuty + nseTransactionCharge + bseTransactionCharge + sebiTurnoverFee + gst) from TradeLedger tl where tl.transactionDate BETWEEN :dateFrom AND :dateTo AND tl.userId = :userId")
	Double getNetTaxPaidBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	
	@Query(value = "SELECT sum(tl.brokerage) from TradeLedger tl where tl.transactionDate BETWEEN :dateFrom AND :dateTo AND tl.userId = :userId")
	Double getBrokeragePaidBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	
}
