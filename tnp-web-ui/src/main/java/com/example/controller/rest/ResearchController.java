package com.example.controller.rest;

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
	
	@GetMapping(value="/current", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getResearchStocks() {

		List<ResearchLedger> researchList = researchLedgerService.researchStocks();
		
		return ResponseEntity.ok(uiRenderUtil.renderResearchList(researchList));
	}
	
	
}
