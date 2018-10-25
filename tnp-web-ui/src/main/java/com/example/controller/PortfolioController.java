package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.controller.model.Person;
import com.example.model.master.Stock;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

	private static List<Stock> stockList = new ArrayList<>();
	
	static {
		stockList.add(new Stock("ABC", "ABC", "25.23"));
		stockList.add(new Stock("XYZ", "XYZ", "19.23"));
		stockList.add(new Stock("DEF", "DEF", "25.23"));
		stockList.add(new Stock("ABC1", "ABC", "20.23"));
		stockList.add(new Stock("XYZ1", "XYZ", "15.23"));
		stockList.add(new Stock("DEF1", "DEF", "13.23"));
		stockList.add(new Stock("ABC2", "ABC", "25.23"));
		stockList.add(new Stock("XYZ2", "XYZ", "10.23"));
		stockList.add(new Stock("DEF2", "DEF", "15.23"));
		stockList.add(new Stock("ABC3", "ABC", "25.23"));
		stockList.add(new Stock("XYZ3", "XYZ", "35.23"));
		stockList.add(new Stock("DEF3", "DEF", "25.23"));
	}
	
	
	@GetMapping(value="/", produces = MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<List<Stock>> getStudentDetails() {

		return ResponseEntity.ok(stockList);
	}
	
	@PostMapping(value = "/saveContact", consumes = MediaType.APPLICATION_JSON_VALUE)
	 public ResponseEntity<?> saveContact(@RequestBody Person person) {
		
		System.out.println(person);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	 }
}
