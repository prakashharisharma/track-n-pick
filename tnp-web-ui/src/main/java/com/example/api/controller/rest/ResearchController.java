package com.example.api.controller.rest;

import java.util.List;

import com.example.model.um.UserProfile;
import com.example.security.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.ResearchLedgerTechnicalService;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerService;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerTechnicalService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private UiRenderUtil uiRenderUtil;
	
	@GetMapping(value="/fundamental", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getFundamentalResearchStocks() {

		List<ResearchLedgerFundamental> researchList = researchLedgerService.researchStocksFundamentals();
		
		return ResponseEntity.ok(uiRenderUtil.renderResearchList(researchList));
	}
	
	@GetMapping(value="/technical", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getTechnicalResearchStocks() {

		List<ResearchLedgerTechnical> researchList = researchLedgerTechnicalService.researchStocksTechnicals();

		UserProfile userProfile = loginService.getLoginUserProfile();

		return ResponseEntity.ok(uiRenderUtil.renderResearchTechnicalList(userProfile, researchList));
	}
	
	@GetMapping(value="/fundamental/advance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getAdvancedFundamentalResearchStocks() {

		List<ResearchLedgerFundamental> researchList = researchLedgerService.researchStocksFundamentals();
		
		return ResponseEntity.ok(uiRenderUtil.renderAdvancedResearchList(researchList));
	}
	
	@GetMapping(value="/technical/advance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getAdvancedTechnicalResearchStocks() {

		List<ResearchLedgerTechnical> researchList = researchLedgerTechnicalService.researchStocksTechnicals();
		
		return ResponseEntity.ok(uiRenderUtil.renderAdvancedResearchTechnicalList(researchList));
	}
}
