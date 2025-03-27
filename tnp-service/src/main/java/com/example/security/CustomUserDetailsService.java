package com.example.security;


import com.example.model.um.Role;
import com.example.repo.um.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.example.model.um.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.error("DEBUG: loadUserByUsername called for: " + username);
        //throw new RuntimeException("DEBUG: Checking if this method is being called!");


        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Stored encrypted in DB
                .accountExpired(user.isAccountExpired())
                .accountLocked(user.isAccountLocked())
                .credentialsExpired(user.isCredentialsExpired())
                .disabled(!user.isEnabled())
                .roles(user.getRoles().stream().map(Role::name) // Convert Enum to String
                .toArray(String[]::new))
                .build();
    }
}
