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

    private String handle;
    private String firstName;
    private String lastName;

    public LoginDetails(UserDetails userDetails, String firstName, String lastName, String handle) {
        this.email = userDetails.getUsername();
        this.password = userDetails.getPassword();
        this.authorities = userDetails.getAuthorities();

        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}
