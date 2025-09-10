package com.zedapps.bookshare.dto.login;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Getter
@Setter
public class LoginDetails implements UserDetails {

    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    private String name;

    public LoginDetails(UserDetails userDetails, String name) {
        this.email = userDetails.getUsername();
        this.password = userDetails.getPassword();
        this.authorities = userDetails.getAuthorities();

        this.name = name;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}
