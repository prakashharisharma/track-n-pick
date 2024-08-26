package com.example.api.controller.rest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import com.example.ui.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.security.LoginService;
import com.example.ui.service.UiRenderUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@GetMapping(value="/performance/monthly", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<MonthlyPerformance>> getWeeklyPerformance() {

		List<MonthlyPerformance> result = new ArrayList<>();
		LocalDate date = LocalDate.now().minusDays(5);
		ZoneId zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
		long epoch = date.atStartOfDay(zoneId).toEpochSecond();
		MonthlyPerformance mp = new MonthlyPerformance(date, 1353);
		result.add(mp);

		date = LocalDate.now().minusDays(4);
		zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
		epoch = date.atStartOfDay(zoneId).toEpochSecond();
		mp = new MonthlyPerformance(date, 1353);
		result.add(mp);

		date = LocalDate.now().minusDays(3);
		zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
		epoch = date.atStartOfDay(zoneId).toEpochSecond();
		mp = new MonthlyPerformance(date, 1353);
		result.add(mp);

		date = LocalDate.now().minusDays(2);
		 zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
		 epoch = date.atStartOfDay(zoneId).toEpochSecond();
		 mp = new MonthlyPerformance(date, 1353);
		result.add(mp);

		date = LocalDate.now().minusDays(1);
		zoneId = ZoneId.systemDefault(); // or: ZoneId.of("Europe/Oslo");
		epoch = date.atStartOfDay(zoneId).toEpochSecond();
		mp = new MonthlyPerformance(date, 1353);
		result.add(mp);

		return ResponseEntity.ok(result);
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
