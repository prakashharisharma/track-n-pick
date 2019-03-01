package com.example.api.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.LoginService;
import com.example.storage.service.async.FactorHistoryConsumer;
import com.example.ui.model.UIOverallGainLoss;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	@Autowired
	private UiRenderUtil  uiRenderUtil;

	@Autowired
	private LoginService loginService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);
	
	@GetMapping(value="/performance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<UIOverallGainLoss> getPortfolioStocks() {

		LOGGER.info("REquested");
		
		UIOverallGainLoss uIOverallGainLoss = uiRenderUtil.renderUIPerformance(loginService.getLoginUserProfile());
		
		return ResponseEntity.ok(uIOverallGainLoss);
	}
}
