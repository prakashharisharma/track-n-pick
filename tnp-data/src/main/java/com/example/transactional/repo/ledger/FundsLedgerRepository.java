package com.example.transactional.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import com.example.transactional.model.um.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.FundsLedger;

@Transactional
@Repository
public interface FundsLedgerRepository extends JpaRepository<FundsLedger, Long>{

	List<FundsLedger> findByUserId(User userId);
	
	@Query(value = "SELECT sum(fl.amount) from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId = :userId AND fl.transactionType in ('ADD','FYRO','WITHDRAW')")
	Double getTotalFYFundBetweenTwoDates(@Param("userId") User userId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

	@Query(value = "SELECT sum(fl.amount) from FundsLedger fl where fl.userId = :userId AND fl.transactionType in ('ADD','WITHDRAW')")
	Double getTotalFund(@Param("userId") User userId);

	@Query(value = "SELECT sum(fl.amount) from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId = :userId AND fl.transactionType in ('ADD',  'CYRO')")
	Double getTotalCYFundBetweenTwoDates(@Param("userId") User userId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

	@Query(value = "SELECT fl  from FundsLedger fl where fl.transactionDate BETWEEN :dateFrom AND :dateTo AND fl.userId.id = :userId AND fl.transactionType in ('ADD','WITHDRAW','FYRO')")
	List<FundsLedger> findAllTransactioninCurrentFyByUserId(@Param("userId") long userId, @Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
	
}
