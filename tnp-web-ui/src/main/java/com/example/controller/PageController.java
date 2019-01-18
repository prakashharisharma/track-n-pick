package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ui.model.SignupUser;

@Controller
@RequestMapping("/page")
public class PageController {


	@GetMapping(value = "/dashboard.html")
	public String index(Model model) {
		model.addAttribute("classActiveDashboard", "active");
		return "dashboard";
	}
	
	@GetMapping(value = "/signup.html")
	public String signup(Model model) {
		model.addAttribute("user", new SignupUser());
		return "signup";
	}

	@GetMapping(value = "/research.html")
	public String research(Model model) {
		model.addAttribute("classActiveResearch", "active");
		return "research";
	}

	@GetMapping(value = "/funds.html")
	public String funds(Model model) {
		model.addAttribute("classActiveFunds", "active");
		model.addAttribute("user", new SignupUser());
		return "funds";
	}

	@GetMapping(value = "/home.html")
	public String home(Model model) {
		model.addAttribute("classActiveHome", "active");
		return "home";
	}

	@GetMapping(value = "/index1.html")
	public String index1(Model model) {
		return "index1";
	}

	@GetMapping(value = "/portfolio.html")
	public String portfolioView(Model model) {
		model.addAttribute("classActivePortfolio", "active");
		return "portfolio";
	}

	@GetMapping(value = "/dividends.html")
	public String dividends(Model model) {
		return "dividends";
	}

	@GetMapping(value = "/watchlist.html")
	public String watchListView(Model model) {
		model.addAttribute("classActiveWatchlist", "active");
		return "watchList";
	}

}
