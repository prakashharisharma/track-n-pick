package com.example.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.example.model.um.UserProfile;

public class CurrentUser extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7660469905968480095L;
	
	private UserProfile userProfile;
	
	public CurrentUser(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities, UserProfile userProfile) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		
		this.userProfile = userProfile;
	}

	public CurrentUser(UserProfile userProfile,
			Collection<? extends GrantedAuthority> authorities ) {
		
		super(userProfile.getUserDetails().getUserName(), userProfile.getUserDetails().getPassword(), userProfile.getUserDetails().isEnabled(), !userProfile.getUserDetails().isAccountExpired(),
				!userProfile.getUserDetails().isCredentialsExpired(), !userProfile.getUserDetails().isAccountLocked(), authorities);
		
		this.userProfile = userProfile;
	}
	
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	
}
