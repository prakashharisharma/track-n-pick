package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.FundsLedger;
import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface FundsLedgerRepository extends JpaRepository<FundsLedger, Long>{

	List<FundsLedger> findByUserId(UserProfile userId);
	
	@Query(value = "SELECT sum(fl.amount) from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId = :userId AND fl.transactionType in ('ADD','FYRO')")
	Double getTotalFYFundBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	
	@Query(value = "SELECT sum(fl.amount) from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId = :userId AND fl.transactionType in ('ADD','CYRO')")
	Double getTotalCYFundBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);

	@Query(value = "SELECT fl  from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId.id = :userId AND fl.transactionType in ('ADD','WITHDRAW','FYRO')")
	List<FundsLedger> findAllTransactioninCurrentFyByUserId(@Param("userId") long userId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
	
}
