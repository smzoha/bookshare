package com.zedapps.bookshare.controller.api.dashboard;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.api.dashboard.ReadingStatsDto;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.dashboard.ReadingStatsApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@WebMvcTest(ReadingStatsApiController.class)
@WithMockLoginDetails
public class ReadingStatsApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingStatsApiService readingStatsApiService;

    private ReadingStatsDto readingStatsDto;

    @BeforeEach
    void setUp() {
        // TODO: build a representative ReadingStatsDto (null book DTOs are fine for the shape test)
        readingStatsDto = new ReadingStatsDto(10, 5, 6, 1200L, 7L, 9L,
                Map.of("JANUARY", 1), null, null, null, 3, 4, Map.of(5, 2));
    }

    @Test
    void getReadingStats_authenticated_returns200WithJsonBody() throws Exception {
        when(readingStatsApiService.getReadingStatsDto(any(LoginDetails.class), eq(2026)))
                .thenReturn(readingStatsDto);

        mockMvc.perform(get("/api/v1/readingStats/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challengeBookCount").value(readingStatsDto.challengeBookCount()))
                .andExpect(jsonPath("$.booksReadCount").value(readingStatsDto.booksReadCount()))
                .andExpect(jsonPath("$.totalPagesRead").value(readingStatsDto.totalPagesRead()))
                .andExpect(jsonPath("$.longestBook").isEmpty())
                .andExpect(jsonPath("$.shortestBook").isEmpty())
                .andExpect(jsonPath("$.latestBook").isEmpty())
                .andExpect(jsonPath("$.reviewCount").value(readingStatsDto.reviewCount()))
                .andExpect(jsonPath("$.totalAvgReview").value(readingStatsDto.totalAvgReview()));
    }

    @Test
    void getReadingStats_forwardsYearPathVariableToService() throws Exception {
        when(readingStatsApiService.getReadingStatsDto(any(LoginDetails.class), eq(2025)))
                .thenReturn(readingStatsDto);

        mockMvc.perform(get("/api/v1/readingStats/2025"))
                .andExpect(status().isOk());

        verify(readingStatsApiService).getReadingStatsDto(any(LoginDetails.class), eq(2025));
    }

    @Test
    @WithAnonymousUser
    void getReadingStats_noJwt_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/readingStats/2026"))
                .andExpect(status().isForbidden());

        verify(readingStatsApiService, never()).getReadingStatsDto(any(LoginDetails.class), anyInt());
    }
}
