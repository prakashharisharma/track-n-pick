package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ExpenseLedger;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.ExpenseLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class ExpenseService {

	@Autowired
	private ExpenseLedgerRepository expenseLedgerRepository;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public double currentFinYearExpenses(UserProfile userProfile) {
		Double totalExpense = expenseLedgerRepository.getTotalExpenseBetweenTwoDates(userProfile,
				miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());


		if (totalExpense == null) {
			totalExpense = 0.00;
		}
		
		return totalExpense;
	}
	
	public List<ExpenseLedger> recentExpenses(UserProfile user) {
		return expenseLedgerRepository.findByUserIdOrderByTransactionDateDesc(user);
	}
	
	public void addExpense(UserProfile user, double amount, LocalDate transactionDate, String description) {
		ExpenseLedger el = new ExpenseLedger();
		el.setUserId(user);
		el.setExpenseAmount(amount);
		el.setTransactionDate(transactionDate);
		el.setDescription(description);
		
		expenseLedgerRepository.save(el);
	}

}
