package com.example.api.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.ledger.ResearchLedger;
import com.example.service.ResearchLedgerService;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

	@Autowired
	private ResearchLedgerService researchLedgerService;

	@Autowired
	private UiRenderUtil uiRenderUtil;
	
	@GetMapping(value="/fundamental", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getFundamentalResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocksFundamentals();
		
		return ResponseEntity.ok(uiRenderUtil.renderResearchList(researchList));
	}
	
	@GetMapping(value="/technical", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getTechnicalResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocksTechnicalss();
		
		return ResponseEntity.ok(uiRenderUtil.renderResearchList(researchList));
	}
	
	@GetMapping(value="/fundamental/advance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getAdvancedFundamentalResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocksFundamentals();
		
		return ResponseEntity.ok(uiRenderUtil.renderAdvancedResearchList(researchList));
	}
	
	@GetMapping(value="/technical/advance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getAdvancedTechnicalResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocksTechnicalss();
		
		return ResponseEntity.ok(uiRenderUtil.renderAdvancedResearchList(researchList));
	}
}
