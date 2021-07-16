package com.example.service;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.DownloadLedger;
import com.example.repo.ledger.DownloadLedgerRepository;
import com.example.util.io.model.DownloadTriggerIO.DownloadType;


@Transactional
@Service
public class DownloadLedgerService {

	
	@Autowired
	private DownloadLedgerRepository downloadLedgerRepository;
	
	public void save(DownloadLedger downloadLedger) {
		downloadLedgerRepository.save(downloadLedger);
	}
	
	public boolean isBhavDownloadExist(LocalDate downloadDate) {
		
		boolean isBhavDownloadExist = false;
		DownloadLedger downloadLedger = downloadLedgerRepository.findByDownloadDateAndDownloadType(downloadDate, DownloadType.BHAV);
		
		if(downloadLedger != null) {
			isBhavDownloadExist = true;
		}
		
		return isBhavDownloadExist;
		
	}
}
