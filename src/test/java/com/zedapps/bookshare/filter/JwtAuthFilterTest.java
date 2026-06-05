package com.zedapps.bookshare.filter;

import com.zedapps.bookshare.service.auth.JwtService;
import com.zedapps.bookshare.service.auth.LoginDetailService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 5/6/26
 **/
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginDetailService loginDetailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_continuesChainWithoutSettingAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, loginDetailService);
    }

    @Test
    void doFilterInternal_authHeaderNotBearer_continuesChainWithoutSettingAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, loginDetailService);
    }

    @Test
    void doFilterInternal_validToken_setsAuthenticationInSecurityContext() throws ServletException, IOException {
        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.jwt");
        when(jwtService.getEmail("valid.token.jwt")).thenReturn("test@test.com");
        when(loginDetailService.loadUserByUsername("test@test.com")).thenReturn(loginDetails);
        when(jwtService.isTokenValid("valid.token.jwt", loginDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(loginDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws ServletException, IOException {
        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.jwt");
        when(jwtService.getEmail("invalid.token.jwt")).thenReturn("test@test.com");
        when(loginDetailService.loadUserByUsername("test@test.com")).thenReturn(loginDetails);
        when(jwtService.isTokenValid("invalid.token.jwt", loginDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_authenticationAlreadySet_skipsTokenProcessing() throws ServletException, IOException {
        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        when(request.getHeader("Authorization")).thenReturn("Bearer sample.token.jwt");
        when(jwtService.getEmail("sample.token.jwt")).thenReturn("test@test.com");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(loginDetailService, never()).loadUserByUsername(any());
        verify(jwtService, never()).isTokenValid(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsWebAuthenticationDetails() throws ServletException, IOException {
        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.jwt");
        when(jwtService.getEmail("valid.token.jwt")).thenReturn("test@test.com");
        when(loginDetailService.loadUserByUsername("test@test.com")).thenReturn(loginDetails);
        when(jwtService.isTokenValid("valid.token.jwt", loginDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getDetails())
                .isInstanceOf(WebAuthenticationDetails.class);
    }
}
