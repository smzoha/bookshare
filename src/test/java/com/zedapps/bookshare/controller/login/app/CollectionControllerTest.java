package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 30/5/26
 **/
@WebMvcTest(CollectionController.class)
@WithMockLoginDetails
public class CollectionControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private ShelfService shelfService;

    private Login login;
    private Shelf defaultShelf;
    private Shelf customShelf;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);

        defaultShelf = TestUtils.getShelf(login, "Default Shelf", true);
        defaultShelf.setId(1L);

        customShelf = TestUtils.getShelf(login, "Custom Shelf", false);
        customShelf.setId(2L);

        when(loginService.getLogin(login.getEmail())).thenReturn(login);
        when(shelfService.getShelvesForCollection(login.getEmail()))
                .thenReturn(List.of(defaultShelf, customShelf));
    }

    @Test
    void showCollection_authenticatedUser_loadsShelvesAndDefaultsToFirstShelf() throws Exception {
        mockMvc.perform(get("/collection"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("login", "collections", "currentShelf"))
                .andExpect(model().attribute("login", login))
                .andExpect(model().attribute("collections", List.of(defaultShelf, customShelf)))
                .andExpect(model().attribute("currentShelf", defaultShelf))
                .andExpect(view().name("app/login/collection"));

        verify(loginService).getLogin(login.getEmail());
        verify(shelfService).getShelvesForCollection(login.getEmail());
    }

    @Test
    void showCollection_withShelfIdParam_loadsSpecifiedShelf() throws Exception {
        when(shelfService.getShelfById(customShelf.getId())).thenReturn(customShelf);

        mockMvc.perform(get("/collection")
                        .param("shelfId", customShelf.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("login", "collections", "currentShelf"))
                .andExpect(model().attribute("login", login))
                .andExpect(model().attribute("collections", List.of(defaultShelf, customShelf)))
                .andExpect(model().attribute("currentShelf", customShelf));

        verify(loginService).getLogin(login.getEmail());
        verify(shelfService).getShelvesForCollection(login.getEmail());
        verify(shelfService).getShelfById(customShelf.getId());
    }

    @Test
    void showCollection_unownedShelfId_throwsAssertionError() {
        Login otherLogin = TestUtils.getLogin("other@test.com", "other", true);
        Shelf otherShelf = TestUtils.getShelf(otherLogin, "Other Shelf", true);
        otherShelf.setId(3L);

        when(shelfService.getShelfById(otherShelf.getId())).thenReturn(otherShelf);

        assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/collection")
                        .param("shelfId", otherShelf.getId().toString())));

        verify(loginService).getLogin(login.getEmail());
        verify(shelfService).getShelvesForCollection(login.getEmail());
        verify(shelfService).getShelfById(otherShelf.getId());
    }

    @Test
    void showCollection_ajaxRequest_returnsFragment() throws Exception {
        mockMvc.perform(get("/collection")
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("login", "collections", "currentShelf"))
                .andExpect(view().name("app/common/shelvedBookFragment :: shelvedBooks"));
    }
}
