package com.taxirank.backend.services;

import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * People log in with their email, rank accounts with their rank code.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(loginName)
                .or(() -> userRepository.findByUsernameIgnoreCase(loginName))
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + loginName));
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
