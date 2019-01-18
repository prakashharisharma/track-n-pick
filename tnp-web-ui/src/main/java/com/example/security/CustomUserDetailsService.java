package com.example.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.um.Role;
import com.example.model.um.UserProfile;
import com.example.service.UserService;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService{

	@Autowired
	private UserService userService;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		
		com.example.model.um.UserProfile user = userService.getUserByUsername(username);
		
		if(user == null) {
			Set<Role> anonymousRoles = new HashSet<Role>(0);
			
			Role anonymousRole = new Role();
			anonymousRole.setRole("anonymous");
			
			anonymousRoles.add(anonymousRole);
			
			List<GrantedAuthority> authorities = buildUserAuthority(anonymousRoles);
			
			return buildAnonymousUserForAuthentication(authorities);
		}
		
		List<GrantedAuthority> authorities = buildUserAuthority(user.getRoles());
		return buildUserForAuthentication(user, authorities);

	}

	private User buildAnonymousUserForAuthentication(List<GrantedAuthority> authorities) {

		return new CurrentUser("anonymousUser", "", false, false,
				false, false, authorities, null);
	}
	
	// Converts com.td.sb.model.User user to
	// org.springframework.security.core.userdetails.User
	private User buildUserForAuthentication(UserProfile userProfile, List<GrantedAuthority> authorities) {

	/*	return new CurrentUser(userProfile.getUserDetails().getUserName(), userProfile.getUserDetails().getPassword(), user.getUserDetails().isEnabled(), !user.getUserDetails().isAccountExpired(),
				!user.getUserDetails().isCredentialsExpired(), !user.getUserDetails().isAccountLocked(), authorities, userProfile);*/
		return new CurrentUser(userProfile, authorities);
	}

	private List<GrantedAuthority> buildUserAuthority(Set<Role> userRoles) {

		Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

		// Build user's authorities
		for (Role userRole : userRoles) {
			setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
						
		}

		List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

		return Result;
	}
}
