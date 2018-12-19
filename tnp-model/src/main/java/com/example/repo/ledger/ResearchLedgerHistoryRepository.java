package com.example.repo.ledger;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.ledger.ResearchLedgerHistory;

@Transactional
@Repository
public interface ResearchLedgerHistoryRepository extends JpaRepository<ResearchLedgerHistory, Long> {

	List<ResearchLedgerHistory> findAll();
	
	
}
