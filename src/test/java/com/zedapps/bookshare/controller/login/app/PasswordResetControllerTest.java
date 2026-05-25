package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.config.SecurityConfig;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.JwtService;
import com.zedapps.bookshare.service.auth.LoginDetailOidcService;
import com.zedapps.bookshare.service.auth.LoginDetailService;
import com.zedapps.bookshare.service.login.PasswordResetService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 23/5/26
 **/
@WebMvcTest(PasswordResetController.class)
@Import(SecurityConfig.class)
public class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private LoginRepository loginRepository;

    @MockitoBean
    private LoginDetailService loginDetailService;

    @MockitoBean
    private LoginDetailOidcService loginDetailOidcService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockLoginDetails
    void showResetRequestForm_authenticatedUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/resetPasswordRequest"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockLoginDetails
    void showResetForm_authenticatedUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/resetPassword")
                        .param("token", "test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void showResetRequestForm_always_returnsResetRequestView() throws Exception {
        mockMvc.perform(get("/resetPasswordRequest"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/resetPasswordRequest"));
    }

    @Test
    @WithAnonymousUser
    void submitResetRequest_knownEmail_savesTokenAndRedirectsWithSuccessFlash() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);
        when(loginRepository.findActiveLoginByEmail(eq("test@test.com")))
                .thenReturn(Optional.of(login));

        mockMvc.perform(post("/resetPasswordRequest")
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("passwordResetReqSuccess", true));

        verify(passwordResetService).savePasswordResetToken(eq(login.getEmail()));
    }

    @Test
    @WithAnonymousUser
    void submitResetRequest_unknownEmail_rendersRequestFormWithError() throws Exception {
        when(loginRepository.findActiveLoginByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/resetPasswordRequest")
                        .param("email", "invalid@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/resetPasswordRequest"))
                .andExpect(model().attribute("error", true));

        verify(passwordResetService, never()).savePasswordResetToken(anyString());
    }

    @Test
    @WithAnonymousUser
    void showResetForm_validToken_returnsResetFormView() throws Exception {
        mockMvc.perform(get("/resetPassword")
                        .param("token", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/resetPassword"))
                .andExpect(model().attributeExists("passwordResetDto"))
                .andExpect(model().attribute("passwordResetDto", hasProperty("token", equalTo("test"))));

        verify(passwordResetService).validateToken(eq("test"));
    }

    @Test
    @WithAnonymousUser
    void showResetForm_invalidToken_throwsServletException() throws Exception {
        doThrow(IllegalArgumentException.class).when(passwordResetService).validateToken(anyString());

        assertThrows(ServletException.class, () -> mockMvc.perform(get("/resetPassword")
                .param("token", "test")));

        verify(passwordResetService).validateToken(eq("test"));
    }

    @Test
    @WithAnonymousUser
    void submitResetForm_validDto_resetsPasswordAndRedirects() throws Exception {
        mockMvc.perform(post("/resetPassword")
                        .param("token", "test")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("passwordResetSuccess", true));

        verify(passwordResetService).validatePasswordResetDto(any(PasswordResetDto.class), any(Errors.class));
        verify(passwordResetService).resetPassword(any(PasswordResetDto.class));
    }

    @Test
    @WithAnonymousUser
    void submitResetForm_validationErrors_returnsResetFormView() throws Exception {
        mockMvc.perform(post("/resetPassword")
                        .param("token", "test")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/resetPassword"));

        verify(passwordResetService).validatePasswordResetDto(any(PasswordResetDto.class), any(Errors.class));
        verify(passwordResetService, never()).resetPassword(any(PasswordResetDto.class));
    }

    @Test
    @WithAnonymousUser
    void submitResetForm_passwordMismatch_returnsResetFormViewWithErrors() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("password", "error.password.do.not.match");

            return null;
        }).when(passwordResetService).validatePasswordResetDto(any(PasswordResetDto.class), any(Errors.class));

        mockMvc.perform(post("/resetPassword")
                        .param("token", "test")
                        .param("password", "password")
                        .param("confirmPassword", "password1"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/resetPassword"))
                .andExpect(model().attributeHasFieldErrorCode("passwordResetDto", "password", "error.password.do.not.match"));
    }
}
