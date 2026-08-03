package com.apliman.auth_service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.apliman.auth_service.model.UserPrincipal;
import com.apliman.auth_service.repository.UserRepository;

@Service
public class AuthPrincipal implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public AuthPrincipal(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
        return new UserPrincipal(user);
    }
}
