package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping(value = "/")
	 public String hello(Model model) {
		model.addAttribute("msg", "Hello, This message has been injected from the controller method");
		return "index";
	 }
	
	
	@GetMapping(value = "/index1")
	 public String index1(Model model) {
		return "index1";
	 }
	
	@GetMapping(value = "/view_portfolio")
	 public String portfolioView(Model model) {
		return "portfolio";
	 }
	
}
