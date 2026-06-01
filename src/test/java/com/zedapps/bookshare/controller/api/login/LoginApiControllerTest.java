package com.zedapps.bookshare.controller.api.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.service.login.LoginApiService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 29/5/26
 **/
@WebMvcTest(LoginApiController.class)
@WithAnonymousUser
public class LoginApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginApiService loginApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginApiDto loginApiDto;
    private RegistrationRequestDto registrationRequestDto;
    private PasswordResetDto passwordResetDto;

    @BeforeEach
    void setUp() {
        Login login = TestUtils.getLogin("test@test.com", "test", true);
        registrationRequestDto = TestUtils.getRegistrationRequestDto(login);

        loginApiDto = new LoginApiDto(login.getFirstName(), login.getLastName(), login.getHandle(),
                login.getEmail(), null, null, login.getRole().name(), login.isActive(),
                List.of(), List.of(), List.of());

        passwordResetDto = new PasswordResetDto("token");
        passwordResetDto.setPassword("password");
        passwordResetDto.setConfirmPassword("password");
    }

    @Test
    void register_validRequest_returns200WithLoginApiDto() throws Exception {
        when(loginApiService.registerLogin(any(RegistrationRequestDto.class)))
                .thenReturn(loginApiDto);

        mockMvc.perform(post("/api/v1/login/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registrationRequestDto.getEmail()))
                .andExpect(jsonPath("$.handle").value(registrationRequestDto.getHandle()))
                .andExpect(jsonPath("$.firstName").value(registrationRequestDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(registrationRequestDto.getLastName()))
                .andExpect(jsonPath("$.role").value(Role.USER.name()))
                .andExpect(jsonPath("$.active").value(true));

        verify(loginApiService).validateRegistration(any(RegistrationRequestDto.class), any(Errors.class));
        verify(loginApiService).registerLogin(any(RegistrationRequestDto.class));
    }

    @Test
    void register_bindingErrors_returns400WithErrorResponseDto() throws Exception {
        registrationRequestDto.setEmail(null);

        mockMvc.perform(post("/api/v1/login/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());

        verify(loginApiService, never()).registerLogin(any(RegistrationRequestDto.class));
    }

    @Test
    void register_customValidationErrors_returns400WithErrorResponseDto() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "error.email.exists");

            return null;
        }).when(loginApiService).validateRegistration(any(RegistrationRequestDto.class),
                any(Errors.class));

        mockMvc.perform(post("/api/v1/login/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());

        verify(loginApiService).validateRegistration(any(RegistrationRequestDto.class),
                any(Errors.class));

        verify(loginApiService, never()).registerLogin(any(RegistrationRequestDto.class));
    }

    @Test
    void requestPasswordReset_knownEmail_returns200() throws Exception {
        when(loginApiService.saveResetPasswordToken("test@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/v1/login/resetPassword/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@test.com"))))
                .andExpect(status().isOk());

        verify(loginApiService).saveResetPasswordToken("test@test.com");
    }

    @Test
    void requestPasswordReset_unknownEmail_returns400WithErrorCode() throws Exception {
        when(loginApiService.saveResetPasswordToken("test@test.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/login/resetPassword/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@test.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors").isNotEmpty())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.invalid.email"));

        verify(loginApiService).saveResetPasswordToken("test@test.com");
    }

    @Test
    void requestPasswordReset_missingEmail_returns400() throws Exception {
        when(loginApiService.saveResetPasswordToken("")).thenReturn(false);

        mockMvc.perform(post("/api/v1/login/resetPassword/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors").isNotEmpty())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.invalid.email"));

        verify(loginApiService).saveResetPasswordToken("");
    }

    @Test
    void resetPassword_validDto_returns200() throws Exception {
        when(loginApiService.resetPassword(any(PasswordResetDto.class),
                any(Errors.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/login/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isOk());

        verify(loginApiService).resetPassword(any(PasswordResetDto.class), any(Errors.class));
    }

    @Test
    void resetPassword_validationErrors_returns400WithErrorResponseDto() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.reject("error.invalid");

            return false;
        }).when(loginApiService).resetPassword(any(PasswordResetDto.class),
                any(Errors.class));

        mockMvc.perform(post("/api/v1/login/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors").isNotEmpty())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.invalid"));

        verify(loginApiService).resetPassword(any(PasswordResetDto.class), any(Errors.class));
    }

    @Test
    void resetPassword_bindingErrors_returns400() throws Exception {
        passwordResetDto.setConfirmPassword(null);
        when(loginApiService.resetPassword(any(PasswordResetDto.class),
                any(Errors.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/login/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());

        verify(loginApiService).resetPassword(any(PasswordResetDto.class), any(Errors.class));
    }
}
