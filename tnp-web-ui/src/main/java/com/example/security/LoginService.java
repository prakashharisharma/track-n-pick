package com.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.model.um.UserProfile;

@Service
public class LoginService {

	public CurrentUser getLoginUser() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
		
		return currentUser;

	}
	
	public UserProfile getLoginUserProfile() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
		
		return currentUser.getUserProfile();

	}

	public String getLoginUserFirstName(){

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (!"anonymousUser".equalsIgnoreCase(auth.getPrincipal().toString())) {
			CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
			return currentUser.getUserProfile().getFirstName();
		} else {
			return auth.getPrincipal().toString();
		}

	}
}
