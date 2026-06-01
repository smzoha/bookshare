package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.Utils;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 22/5/26
 **/
@WebMvcTest(LoginController.class)
public class LoginControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private LoginDtoValidator loginDtoValidator;

    @Test
    @WithAnonymousUser
    void showLoginPage_notAuthenticated_returnsLoginView() throws Exception {
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(Utils::isAuthenticated).thenReturn(false);

            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("app/login/login"))
                    .andExpect(model().attributeExists("login", "register"));
        }
    }

    @Test
    @WithMockLoginDetails
    void showLoginPage_alreadyAuthenticated_redirectsToHome() throws Exception {
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class)) {
            utils.when(Utils::isAuthenticated).thenReturn(true);

            mockMvc.perform(get("/login"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    @WithAnonymousUser
    void register_validRequest_createsAccountAndRedirects() throws Exception {
        ArgumentCaptor<RegistrationRequestDto> captor = ArgumentCaptor.forClass(RegistrationRequestDto.class);

        mockMvc.perform(post("/register")
                        .param("email", "test@test.com")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("handle", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(loginDtoValidator).validate(any(RegistrationRequestDto.class), any(Errors.class));
        verify(loginService).createLogin(captor.capture());

        assertEquals("test@test.com", captor.getValue().getEmail());
        assertEquals("test", captor.getValue().getHandle());
    }

    @Test
    @WithAnonymousUser
    void register_validationErrors_returnsLoginViewWithErrors() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/login"))
                .andExpect(model().attributeExists("login", "register"))
                .andExpect(model().attributeHasFieldErrors("register", "password"));

        verify(loginService, never()).createLogin(any(RegistrationRequestDto.class));
    }

    @Test
    @WithAnonymousUser
    void register_customValidationErrors_returnsLoginViewWithErrors() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "error.email.exists");

            return null;
        }).when(loginDtoValidator).validate(any(RegistrationRequestDto.class), any(Errors.class));

        mockMvc.perform(post("/register")
                        .param("email", "test@test.com")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("handle", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/login"))
                .andExpect(model().attributeExists("login", "register"))
                .andExpect(model().attributeHasFieldErrors("register", "email"));

        verify(loginService, never()).createLogin(any(RegistrationRequestDto.class));
    }

    @Test
    @WithAnonymousUser
    void register_confirmPasswordMismatch_returnsLoginViewWithErrors() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("confirmPassword", "error.password.do.not.match");

            return null;
        }).when(loginDtoValidator).validate(any(RegistrationRequestDto.class), any(Errors.class));

        mockMvc.perform(post("/register")
                        .param("email", "test@test.com")
                        .param("password", "password")
                        .param("confirmPassword", "different")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("handle", "test"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/login"))
                .andExpect(model().attributeExists("login", "register"))
                .andExpect(model().attributeHasFieldErrors("register", "confirmPassword"));

        verify(loginService, never()).createLogin(any(RegistrationRequestDto.class));
    }
}
