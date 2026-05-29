package com.zedapps.bookshare.controller.api.login;

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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author smzoha
 * @since 29/5/26
 **/
@WebMvcTest(AuthorApiController.class)
@WithMockLoginDetails(email = "test@test.com")
public class AuthorApiControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorRequestService authorRequestService;

    @Test
    void applyForAuthor_eligibleUser_returns200() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);

        when(authorRequestService.getValidLoginForRequest(eq("test@test.com")))
                .thenReturn(login);

        mockMvc.perform(post("/api/v1/author/apply"))
                .andExpect(status().isOk());

        verify(authorRequestService).saveAuthorRequest(login);
    }

    @Test
    void applyForAuthor_ineligibleUser_returns400() throws Exception {
        when(authorRequestService.getValidLoginForRequest(eq("test@test.com"))).thenReturn(null);

        mockMvc.perform(post("/api/v1/author/apply"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.globalErrors").isNotEmpty())
                .andExpect(jsonPath("$.globalErrors[0]").value("error.invalid"));

        verify(authorRequestService).getValidLoginForRequest(eq("test@test.com"));
        verify(authorRequestService, never()).saveAuthorRequest(any());
    }
}
