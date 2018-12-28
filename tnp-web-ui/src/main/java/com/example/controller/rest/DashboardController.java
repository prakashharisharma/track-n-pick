package com.example.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.um.User;
import com.example.service.UserService;
import com.example.ui.model.UIOverallGainLoss;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private UiRenderUtil  uiRenderUtil;

	
	@GetMapping(value="/performance", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<UIOverallGainLoss> getPortfolioStocks() {

		User user = userService.getUserById(1);
		
		UIOverallGainLoss uIOverallGainLoss = uiRenderUtil.renderUIPerformance(user);
		
		return ResponseEntity.ok(uIOverallGainLoss);
	}
}
