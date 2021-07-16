package com.example.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ExpenseLedger;
import com.example.model.um.UserProfile;

@Transactional
@Repository
public interface ExpenseLedgerRepository extends JpaRepository<ExpenseLedger, Long> {

	@Query(value = "SELECT sum(el.expenseAmount) from ExpenseLedger el where el.transactionDate BETWEEN :dateFrom AND :dateTo AND el.userId = :userId")
	Double getTotalExpenseBetweenTwoDates(@Param("userId") UserProfile userId,@Param("dateFrom") LocalDate dateFrom,@Param("dateTo") LocalDate dateTo);
	
	List<ExpenseLedger> findByUserIdOrderByTransactionDateDesc(UserProfile userId);
	
}
