package com.zedapps.bookshare.controller.api.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.book.ReadingProgressDto;
import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.ProfileApiService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 30/5/26
 **/
@WebMvcTest(ProfileApiController.class)
@WithMockLoginDetails(email = "test@test.com")
public class ProfileApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileApiService profileApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private Login login;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
    }

    @Test
    void getProfile_existingHandle_returns200WithLoginApiDto() throws Exception {
        LoginApiDto loginApiDto = new LoginApiDto(login.getFirstName(), login.getLastName(), login.getHandle(),
                login.getEmail(), null, null, login.getRole().name(), login.isActive(),
                null, null, null);

        when(profileApiService.getLogin("test", false)).thenReturn(loginApiDto);

        mockMvc.perform(get("/api/v1/profile/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(login.getEmail()))
                .andExpect(jsonPath("$.handle").value(login.getHandle()));

        verify(profileApiService).getLogin("test", false);
    }

    @Test
    void getProfile_detailedTrue_passesDetailedFlagToService() throws Exception {
        ShelfDto shelfDto = new ShelfDto("Test Shelf", login.getEmail(), 1, false);
        ReadingProgressDto readingProgressDto = new ReadingProgressDto("Test Book", "ISBN",
                login.getEmail(), 10L, LocalDate.now(), null, false);

        LoginApiDto loginApiDto = new LoginApiDto(login.getFirstName(), login.getLastName(), login.getHandle(),
                login.getEmail(), null, null, login.getRole().name(), login.isActive(),
                List.of(shelfDto), List.of(readingProgressDto), null);

        when(profileApiService.getLogin("test", true)).thenReturn(loginApiDto);

        mockMvc.perform(get("/api/v1/profile/test")
                        .param("detailed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(login.getEmail()))
                .andExpect(jsonPath("$.handle").value(login.getHandle()))
                .andExpect(jsonPath("$.shelfList").isArray())
                .andExpect(jsonPath("$.readingProgress").isArray());

        verify(profileApiService).getLogin("test", true);
    }

    @Test
    void performConnectionAction_validRequest_returns200WithUpdatedDto() throws Exception {
        ConnectionApiDto connectionApiDto = new ConnectionApiDto("friend", ConnectionAction.SEND_FRIEND_REQ);

        mockMvc.perform(post("/api/v1/profile/connect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(connectionApiDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("friend"))
                .andExpect(jsonPath("$.action").value(ConnectionAction.SEND_FRIEND_REQ.name()));

        verify(profileApiService).performConnectionAction(any(LoginDetails.class),
                eq(connectionApiDto));
    }
}
