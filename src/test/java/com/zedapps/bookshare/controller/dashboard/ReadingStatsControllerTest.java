package com.zedapps.bookshare.controller.dashboard;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.helper.ReadingStatsHelper;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 26/6/26
 **/
@WithMockLoginDetails
@WebMvcTest(ReadingStatsController.class)
public class ReadingStatsControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingStatsHelper readingStatsHelper;

    @Test
    void getReadingStats_authenticated_returnsViewAndInvokesHelper() throws Exception {
        stubReferenceDataSetup(null);

        mockMvc.perform(get("/readingStats"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/readingStats"));

        verify(readingStatsHelper).setupReadingStatsReferenceData(any(LoginDetails.class),
                isNull(), anyMap());
    }

    @Test
    void getReadingStats_withYearParam_forwardsYearToHelper() throws Exception {
        stubReferenceDataSetup(2025);

        mockMvc.perform(get("/readingStats")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/login/readingStats"));

        verify(readingStatsHelper).setupReadingStatsReferenceData(any(LoginDetails.class),
                eq(Integer.valueOf(2025)), anyMap());
    }

    @Test
    @WithAnonymousUser
    void getReadingStats_unauthenticated_isRejected() throws Exception {
        mockMvc.perform(get("/readingStats"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    private void stubReferenceDataSetup(Integer year) {
        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        Book longBook = TestUtils.getBook("Longest Book", TestUtils.TEST_ISBN_DATA.getFirst(), author, Status.ACTIVE);
        longBook.setId(1L);

        Book shortBook = TestUtils.getBook("Short Book", TestUtils.TEST_ISBN_DATA.get(1), author, Status.ACTIVE);
        shortBook.setId(2L);

        Book latestBook = TestUtils.getBook("Latest Book", TestUtils.TEST_ISBN_DATA.get(2), author, Status.ACTIVE);
        latestBook.setId(3L);

        doAnswer(invocation -> {
            Map<String, Object> model = invocation.getArgument(2);
            model.put("booksReadMap", Map.of("JANUARY", 1));
            model.put("longestBook", longBook);
            model.put("shortestBook", shortBook);
            model.put("latestBook", latestBook);
            model.put("ratingMap", Map.of(5, 2));

            return null;
        }).when(readingStatsHelper).setupReadingStatsReferenceData(any(LoginDetails.class),
                eq(year), anyMap());
    }
}
