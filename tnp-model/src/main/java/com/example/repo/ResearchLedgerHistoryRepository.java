package com.example.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.ledger.ResearchLedgerHistory;

public interface ResearchLedgerHistoryRepository extends JpaRepository<ResearchLedgerHistory, Long> {

	List<ResearchLedgerHistory> findAll();
	
	
}
