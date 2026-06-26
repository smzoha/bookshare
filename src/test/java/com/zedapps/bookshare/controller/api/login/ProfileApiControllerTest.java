package com.zedapps.bookshare.controller.api.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.book.ReadingProgressDto;
import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeDto;
import com.zedapps.bookshare.dto.api.login.ReadingChallengeRequest;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.ProfileApiService;
import com.zedapps.bookshare.service.login.ReadingChallengeApiService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
@WithMockLoginDetails
public class ProfileApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileApiService profileApiService;

    @MockitoBean
    private ReadingChallengeApiService readingChallengeApiService;

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

    @Test
    void getReadingChallenge_existingChallenge_returns200WithDto() throws Exception {
        ReadingChallengeDto readingChallengeDto = new ReadingChallengeDto(login.getEmail(), 2026, 50);

        when(readingChallengeApiService.getReadingChallenge(login.getEmail()))
                .thenReturn(Optional.of(readingChallengeDto));

        mockMvc.perform(get("/api/v1/profile/readingChallenge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(login.getEmail()))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.bookCount").value(50));

        verify(readingChallengeApiService).getReadingChallenge(login.getEmail());
    }

    @Test
    void getReadingChallenge_noChallenge_returns404() throws Exception {
        when(readingChallengeApiService.getReadingChallenge(login.getEmail()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profile/readingChallenge"))
                .andExpect(status().isNotFound());

        verify(readingChallengeApiService).getReadingChallenge(login.getEmail());
    }

    @Test
    void saveReadingChallenge_validRequest_returns200WithDto() throws Exception {
        ReadingChallengeRequest request = new ReadingChallengeRequest(2026, 50);
        ReadingChallengeDto readingChallengeDto = new ReadingChallengeDto(login.getEmail(), 2026, 50);

        when(readingChallengeApiService.saveReadingChallenge(any(ReadingChallengeRequest.class),
                any(LoginDetails.class))).thenReturn(readingChallengeDto);

        mockMvc.perform(post("/api/v1/profile/readingChallenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(login.getEmail()))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.bookCount").value(50));

        verify(readingChallengeApiService).saveReadingChallenge(any(ReadingChallengeRequest.class),
                any(LoginDetails.class));
    }

    @Test
    void saveReadingChallenge_invalidBookCount_returns400WithErrors() throws Exception {
        ReadingChallengeRequest request = new ReadingChallengeRequest(2026, 0);

        mockMvc.perform(post("/api/v1/profile/readingChallenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bookCount").exists());

        verify(readingChallengeApiService, never()).saveReadingChallenge(any(), any());
    }
}
