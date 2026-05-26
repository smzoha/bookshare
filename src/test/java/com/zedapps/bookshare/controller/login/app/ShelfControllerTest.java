package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 22/5/26
 **/
@WebMvcTest(ShelfController.class)
@WithMockLoginDetails(email = "test@test.com")
public class ShelfControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelfService shelfService;

    @MockitoBean
    private LoginService loginService;

    @Test
    @WithAnonymousUser
    void addShelf_unauthenticatedUser_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/shelf/add")
                        .param("name", "Test Shelf")
                        .param("bookId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void addShelf_newUniqueName_createsShelfAndRedirects() throws Exception {
        Login login = TestUtils.getLogin("test@test.com", "test", true);

        when(shelfService.isShelfExistsForUser("Test Shelf", "test@test.com")).thenReturn(false);
        when(loginService.getLogin("test@test.com")).thenReturn(login);

        mockMvc.perform(post("/shelf/add")
                        .param("name", "Test Shelf")
                        .param("bookId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/1"));

        verify(shelfService).saveShelf(any());
    }

    @Test
    void addShelf_duplicateName_redirectsToBook() throws Exception {
        when(shelfService.isShelfExistsForUser("Test Shelf", "test@test.com")).thenReturn(true);

        mockMvc.perform(post("/shelf/add")
                        .param("name", "Test Shelf")
                        .param("bookId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/book/1"));

        verify(shelfService, never()).saveShelf(any());
        verify(loginService, never()).getLogin(anyString());
    }
}
