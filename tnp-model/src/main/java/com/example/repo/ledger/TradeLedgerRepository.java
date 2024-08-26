package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

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

	@Query( nativeQuery = true, value = "select tl.*  from trade_ledger tl left join user_portfolio up on tl.user_id = up.user_id" +
			" where tl.txn_date >= up.first_txn_date" +
			" and tl.stock_id = up.stock_id" +
			" and tl.user_id = :userId" +
			" and tl.stock_id = :stockId")
	List<TradeLedger> getCashFlows(@Param("userId") UserProfile userId, @Param("stockId") Long stockId);
}
