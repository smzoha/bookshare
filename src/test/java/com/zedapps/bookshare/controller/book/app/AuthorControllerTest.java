package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.AuthorRequestService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 22/5/26
 **/
@WebMvcTest(AuthorController.class)
@WithMockLoginDetails
public class AuthorControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorRequestService authorRequestService;

    @Test
    void applyForAuthor_eligibleUser_returns200() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);
        when(authorRequestService.getValidLoginForRequest("test@test.com")).thenReturn(login);

        mockMvc.perform(post("/author/apply"))
                .andExpect(status().isOk());
    }

    @Test
    void applyForAuthor_ineligibleUser_returns400() throws Exception {
        when(authorRequestService.getValidLoginForRequest("test@test.com")).thenReturn(null);

        mockMvc.perform(post("/author/apply"))
                .andExpect(status().isBadRequest());
    }
}
