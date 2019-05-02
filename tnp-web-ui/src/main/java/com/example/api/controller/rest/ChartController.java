package com.example.api.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.LoginService;
import com.example.ui.model.ChartPerformance;
import com.example.ui.model.ChartType;
import com.example.ui.model.RenderIndiceAllocation;
import com.example.ui.model.RenderSectorWiseValue;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/chart")
public class ChartController {
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private UiRenderUtil  uiRenderUtil;
	
	@GetMapping(value="/performance/currentvalue", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<ChartPerformance>> getCurrentValue() {


		return ResponseEntity.ok(uiRenderUtil.yearlyPerformance(loginService.getLoginUserProfile(), ChartType.PERFORMANCE_CV));
	}
	
	@GetMapping(value="/performance/investmentvalue", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<ChartPerformance>> getPortfolioStocks() {

		return ResponseEntity.ok(uiRenderUtil.yearlyPerformance(loginService.getLoginUserProfile(), ChartType.PERFORMANCE_IV));
	}
	
	@GetMapping(value="/sector/allocation", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<RenderSectorWiseValue>> getSectoralAllocation() {

		
		return ResponseEntity.ok(uiRenderUtil.sectoralAllocation(loginService.getLoginUserProfile()));
	}
	
	@GetMapping(value="/indice/allocation", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<RenderIndiceAllocation>> getIndiceAllocation() {

		return ResponseEntity.ok(uiRenderUtil.indicedAllocation(loginService.getLoginUserProfile()));
	}
	
}
