package com.zedapps.bookshare.controller.login.admin;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.util.TestUtils;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 31/5/26
 **/
@WebMvcTest(LoginAdminController.class)
@WithMockLoginDetails(role = "ADMIN")
@RecordApplicationEvents
public class LoginAdminControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private LoginDtoValidator loginDtoValidator;

    @Autowired
    private ApplicationEvents applicationEvents;

    private List<Login> logins;
    private Login login;

    @BeforeEach
    void setUp() {
        logins = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            Login temp = TestUtils.getLogin(String.format("test%d@test.com", i), String.format("test%d", i), true);
            temp.setId((long) i);

            logins.add(temp);
        }

        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setRole(Role.USER);
        login.setId(15L);

        logins.add(login);
    }

    @Test
    void listUsers_always_returnsUserListView() throws Exception {
        when(loginService.getLoginList()).thenReturn(logins);

        mockMvc.perform(get("/admin/user"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("logins"))
                .andExpect(model().attribute("logins", logins))
                .andExpect(view().name("admin/user/userList"));

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showNewUserForm_always_returnsEmptyFormWithDefaultValues() throws Exception {
        mockMvc.perform(get("/admin/user/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attribute("user", hasProperty("role", equalTo(Role.USER))))
                .andExpect(model().attribute("user", hasProperty("active", equalTo(true))))
                .andExpect(model().attribute("roles", Role.values()))
                .andExpect(view().name("admin/user/userForm"));
    }

    @Test
    void showEditUserForm_existingHandle_returnsPopulatedForm() throws Exception {
        when(loginService.getLoginByHandle(login.getHandle())).thenReturn(login);

        mockMvc.perform(get("/admin/user/" + login.getHandle()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attribute("user", hasProperty("id", equalTo(login.getId()))))
                .andExpect(model().attribute("user", hasProperty("email", equalTo(login.getEmail()))))
                .andExpect(model().attribute("user", hasProperty("handle", equalTo(login.getHandle()))))
                .andExpect(model().attribute("user", hasProperty("firstName", equalTo(login.getFirstName()))))
                .andExpect(model().attribute("user", hasProperty("lastName", equalTo(login.getLastName()))))
                .andExpect(model().attribute("user", hasProperty("role", equalTo(login.getRole()))))
                .andExpect(model().attribute("user", hasProperty("active", equalTo(login.isActive()))))
                .andExpect(model().attribute("roles", Role.values()))
                .andExpect(view().name("admin/user/userForm"));

        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void saveUser_validDto_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/admin/user/save")
                        .param("email", login.getEmail())
                        .param("handle", login.getHandle())
                        .param("firstName", login.getFirstName())
                        .param("lastName", login.getLastName())
                        .param("role", Role.USER.name())
                        .param("active", String.valueOf(login.isActive())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(loginDtoValidator).validate(any(LoginManageDto.class), any(Errors.class));
        verify(loginService).saveLogin(any(LoginManageDto.class));
    }

    @Test
    void saveUser_validationErrors_returnsFormView() throws Exception {
        mockMvc.perform(post("/admin/user/save")
                        .param("email", "")
                        .param("handle", login.getHandle())
                        .param("firstName", login.getFirstName())
                        .param("lastName", login.getLastName())
                        .param("role", Role.USER.name())
                        .param("active", String.valueOf(login.isActive())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attributeHasErrors("user"));

        verify(loginDtoValidator).validate(any(LoginManageDto.class), any(Errors.class));
        verify(loginService, never()).saveLogin(any(LoginManageDto.class));
    }

    @Test
    void saveUser_customValidationErrors_returnsFormView() throws Exception {
        doAnswer(invocation -> {
            Errors errors = invocation.getArgument(1);
            errors.rejectValue("email", "error.email.exists");

            return null;
        }).when(loginDtoValidator).validate(any(LoginManageDto.class), any(Errors.class));

        mockMvc.perform(post("/admin/user/save")
                        .param("email", login.getEmail())
                        .param("handle", login.getHandle())
                        .param("firstName", login.getFirstName())
                        .param("lastName", login.getLastName())
                        .param("role", Role.USER.name())
                        .param("active", String.valueOf(login.isActive())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "error.email.exists"));

        verify(loginDtoValidator).validate(any(LoginManageDto.class), any(Errors.class));
        verify(loginService, never()).saveLogin(any(LoginManageDto.class));
    }
}
