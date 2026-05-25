package com.zedapps.bookshare.controller.api;

import com.zedapps.bookshare.config.SecurityConfig;
import com.zedapps.bookshare.controller.api.login.LoginApiController;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.service.auth.JwtService;
import com.zedapps.bookshare.service.auth.LoginDetailOidcService;
import com.zedapps.bookshare.service.auth.LoginDetailService;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.service.login.FeedApiService;
import com.zedapps.bookshare.service.login.LoginApiService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 25/5/26
 **/
@WebMvcTest({HomeApiController.class, LoginApiController.class})
@Import(SecurityConfig.class)
public class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookRepository bookRepository;

    @MockitoBean
    private BookApiService bookApiService;

    @MockitoBean
    private FeedApiService feedApiService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private LoginApiService loginApiService;

    @MockitoBean
    private LoginDetailService loginDetailService;

    @MockitoBean
    private LoginDetailOidcService loginDetailOidcService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void handleNoResult_noResultException_returns404WithErrorCode() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenThrow(new NoResultException());

        mockMvc.perform(get("/api/v1/home/featured"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.not.found"));
    }

    @Test
    void handleUnreadableMessage_httpMessageNotReadableException_returns400WithErrorCode() throws Exception {
        String content = "{invalid json}";

        mockMvc.perform(post("/api/v1/login/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.invalid.request"));
    }

    @Test
    void handleAccessDeniedException_accessDeniedException_returns403() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenThrow(new AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/api/v1/home/featured"))
                .andExpect(status().isForbidden());
    }

    @Test
    void handleAuthenticationException_authenticationException_returns401() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenThrow(new BadCredentialsException("Unauthorized"));

        mockMvc.perform(get("/api/v1/home/featured"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handleGeneral_runtimeException_returns500WithErrorCode() throws Exception {
        when(bookRepository.getFeaturedBooks()).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/v1/home/featured"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.internal"));
    }
}
