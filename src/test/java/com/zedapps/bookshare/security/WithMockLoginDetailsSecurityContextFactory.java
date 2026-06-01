package com.zedapps.bookshare.security;

import com.zedapps.bookshare.service.auth.LoginDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

/**
 * @author smzoha
 * @since 22/5/26
 **/
public class WithMockLoginDetailsSecurityContextFactory implements WithSecurityContextFactory<WithMockLoginDetails> {

    @Override
    public SecurityContext createSecurityContext(WithMockLoginDetails annotation) {
        LoginDetails principal = new LoginDetails(
                annotation.email(),
                "Test",
                "User",
                annotation.handle(),
                List.of(new SimpleGrantedAuthority(annotation.role())),
                null,
                null
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
