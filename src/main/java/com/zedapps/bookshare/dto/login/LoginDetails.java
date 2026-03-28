package com.zedapps.bookshare.dto.login;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Getter
@Setter
public class LoginDetails implements UserDetails, OidcUser, OAuth2User {

    @Serial
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    private String handle;
    private String firstName;
    private String lastName;

    // OIDC
    private OidcIdToken idToken;
    private OidcUserInfo userInfo;

    public LoginDetails(UserDetails userDetails, String firstName, String lastName, String handle) {
        this.email = userDetails.getUsername();
        this.password = userDetails.getPassword();
        this.authorities = userDetails.getAuthorities();

        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;
    }

    public LoginDetails(String email, String firstName, String lastName, String handle,
                        Collection<GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo) {

        this.email = email;
        this.password = null;
        this.authorities = authorities;

        this.firstName = firstName;
        this.lastName = lastName;
        this.handle = handle;

        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return idToken != null ? idToken.getClaims() : Map.of();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return userInfo != null ? userInfo.getClaims() : Map.of();
    }
}
