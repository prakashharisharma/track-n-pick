package com.example.model.ledger;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.um.UserProfile;

@Entity
@Table(name = "EXPENSE_LEDGER")
public class ExpenseLedger implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8536850210256732402L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "EXPENSE_ID")
	long expenseId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	UserProfile userId;
	
	@Column(name = "EXPENSE_AMOUNT", columnDefinition="Decimal(10,2) default '0.00'")
	double expenseAmount;

	@Column(name = "TXN_DATE")
	LocalDate transactionDate = LocalDate.now();

	@Column(name = "DESCRIPTION")
	String description;
	
	public ExpenseLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserProfile getUserId() {
		return userId;
	}

	public void setUserId(UserProfile userId) {
		this.userId = userId;
	}

	public double getExpenseAmount() {
		return expenseAmount;
	}

	public void setExpenseAmount(double expenseAmount) {
		this.expenseAmount = expenseAmount;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
