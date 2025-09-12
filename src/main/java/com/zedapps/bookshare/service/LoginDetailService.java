package com.zedapps.bookshare.service;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Service
public class LoginDetailService implements UserDetailsService {

    private final LoginRepository loginRepository;

    public LoginDetailService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Login> loginOptional = loginRepository.findActiveLoginByEmail(username);

        if (loginOptional.isPresent()) {
            Login login = loginOptional.get();

            UserDetails userDetails = User.builder()
                    .username(login.getEmail())
                    .password(login.getPassword())
                    .authorities(login.getRole().name())
                    .build();

            return new LoginDetails(userDetails, login.getFirstName(), login.getLastName(), login.getHandle());

        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
