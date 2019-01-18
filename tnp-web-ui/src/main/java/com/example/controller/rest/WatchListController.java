package com.example.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;

@RestController
@RequestMapping("/api/watchlist")
public class WatchListController {

	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UiRenderUtil uiRenderUtil;

	@GetMapping(value="/current", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<UIRenderStock>> getWatchListStocks() {

		UserProfile user = userService.getUserById(1);
		
		List<Stock> userWatchList = watchListService.userWatchList(user);
		
		return ResponseEntity.ok(uiRenderUtil.renderWatchList(userWatchList));
	}
}
