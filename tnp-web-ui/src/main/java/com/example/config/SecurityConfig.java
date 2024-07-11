package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	@Qualifier("customUserDetailsService")
	UserDetailsService userDetailsService;

	@Autowired
	@Qualifier("customAccessDeniedHandler")
	AccessDeniedHandler accessDeniedHandler;
	
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	    authProvider.setUserDetailsService(userDetailsService);
	    authProvider.setPasswordEncoder(passwordEncoder());
	    return authProvider;
	}
	
	@Override
	public void configure(WebSecurity web){
	    web.ignoring().antMatchers("/", "/index", "/js/**", "/css/**","/h2/console","/public/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/page/**", "/api/**").authenticated()
		.and()
        .formLogin()
        .loginPage("/validate/login.html")
        .failureUrl("/validate/loginError.html")
        .defaultSuccessUrl("/page/dashboard.html")
        
      .and()
        .logout()
        .logoutUrl("/validate/logout.html")
        .logoutSuccessUrl("/validate/logoutSuccess.html")
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID")
        .and()
        .exceptionHandling().accessDeniedHandler(accessDeniedHandler);
		
		http.csrf().disable();
		http.headers().frameOptions().disable();
	}
	
	@Bean @Lazy
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
}
