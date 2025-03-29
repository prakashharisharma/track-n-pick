package com.example.transactional.repo.ledger;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transactional.model.ledger.DownloadLedger;
import com.example.util.io.model.DownloadTriggerIO.DownloadType;

@Transactional
@Repository
public interface DownloadLedgerRepository extends JpaRepository<DownloadLedger, Long>{

	DownloadLedger findByDownloadDateAndDownloadType(LocalDate downloadDate, DownloadType downloadType);
	
	List<DownloadLedger> findByDownloadDate(LocalDate downloadDate);
	
	List<DownloadLedger> findByDownloadTypeOrderByDownloadDateDesc(DownloadType downloadType);
}
