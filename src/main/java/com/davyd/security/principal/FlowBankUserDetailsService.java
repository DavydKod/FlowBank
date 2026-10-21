package com.davyd.security.principal;

import com.davyd.auth.UserCredentials;
import com.davyd.auth.UserCredentialsRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class FlowBankUserDetailsService implements UserDetailsService {
    private final UserCredentialsRepository userCredentialsRepository;

    public FlowBankUserDetailsService(UserCredentialsRepository userCredentialsRepository){
        this.userCredentialsRepository = userCredentialsRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentials userCredentials = userCredentialsRepository.findByUser_Email(email)
                .orElseThrow(() -> new UsernameNotFoundException("User under email " + email +
                        " not found"));
        return new FlowBankPrincipal(
                userCredentials.getUser().getId(),
                email,
                userCredentials.getPasswordHash()
        );
    }
}
