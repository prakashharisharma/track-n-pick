package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.TradeProfitLedger;
import com.example.model.um.UserProfile;

@Repository
public interface TradeProfitLedgerRepository extends JpaRepository<TradeProfitLedger, Long> {

	List<TradeProfitLedger> findByUserId(UserProfile userId);
	
	@Query(value = "SELECT sum(tfl.netProfit) from TradeProfitLedger tfl where tfl.transactionDate BETWEEN :dateFrom AND :dateTo AND tfl.userId = :userId")
	Double getTotalProfitBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	

}
