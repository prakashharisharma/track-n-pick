package com.example.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/validate")
public class LoginController {


	@GetMapping(value = "/login.html")
	public String login(Model model) {

		return "login";
	}

	@GetMapping(value = "/loginError.html")
	public String loginError(Model model) {

		model.addAttribute("loginError", "loginError");
		return "login";
	}

	@GetMapping(value = "/logoutSuccess.html")
	public String logout(Model model) {

		model.addAttribute("logout", "logout");
		return "login";
	}

	@GetMapping(value = "/accessDenied.html")
	public String accessDenied(Model model) {

		model.addAttribute("accessDenied", "accessDenied");
		return "accessDenied";
	}


}
