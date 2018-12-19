package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.ui.model.SignupUser;

@Controller
public class IndexController {

	@GetMapping(value = "/")
	 public String hello(Model model) {
		model.addAttribute("msg", "Hello, This message has been injected from the controller method");
		return "index";
	 }
	
	@GetMapping(value = "/no")
	 public String notsignedin(Model model) {
		model.addAttribute("msg", "Hello, This message has been injected from the controller method");
		return "homeNotSignedIn";
	 }
	
	@GetMapping(value = "/login")
	 public String login(Model model) {
		model.addAttribute("msg", "Hello, This message has been injected from the controller method");
		return "login";
	 }
	@GetMapping(value = "/signup")
	 public String signup(Model model) {
		model.addAttribute("user", new SignupUser());
		return "signup";
	 }
	
	@GetMapping(value = "/home")
	 public String home(Model model) {
		return "home";
	 }
	
	@GetMapping(value = "/index1")
	 public String index1(Model model) {
		return "index1";
	 }
	
	@GetMapping(value = "/portfolio")
	 public String portfolioView(Model model) {
		return "portfolio";
	 }
	@GetMapping(value = "/watchlist")
	 public String watchListView(Model model) {
		return "watchList";
	 }
}
