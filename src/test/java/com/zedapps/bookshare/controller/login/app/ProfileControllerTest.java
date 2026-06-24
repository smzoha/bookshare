package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.helper.ProfileHelper;
import com.zedapps.bookshare.repository.login.ReadingChallengeRepository;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 30/5/26
 **/
@WebMvcTest(ProfileController.class)
@WithMockLoginDetails
@RecordApplicationEvents
public class ProfileControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileHelper profileHelper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private ReadingChallengeRepository readingChallengeRepository;

    @MockitoBean
    private LoginService loginService;

    @Autowired
    private ApplicationEvents applicationEvents;

    private Login login;
    private Login otherLogin;

    private Shelf shelf;
    private Shelf otherShelf;

    private ReadingChallenge readingChallenge;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        otherLogin = TestUtils.getLogin("other@test.com", "other", true);

        shelf = TestUtils.getShelf(login, "Read", true);
        shelf.setId(1L);

        otherShelf = TestUtils.getShelf(otherLogin, "Read", true);
        otherShelf.setId(2L);

        login.setShelves(Set.of(shelf));
        otherLogin.setShelves(Set.of(otherShelf));

        readingChallenge = new ReadingChallenge(login, 2026, 10);

        lenient().when(loginService.getLoginByHandle(login.getHandle())).thenReturn(login);
        lenient().when(loginService.getLogin(login.getEmail())).thenReturn(login);
    }

    @Test
    void redirectToOwnProfile_authenticatedUser_redirectsToHandle() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/test"));
    }

    @Test
    void showProfile_existingHandle_returnsProfileView() throws Exception {
        doAnswer(invocation -> {
            populateReferenceData(invocation.getArgument(2), login, true);

            return null;
        }).when(profileHelper).setupReferenceData(anyString(), any(LoginDetails.class), any(ModelMap.class));

        doAnswer(invocation -> {
            ModelMap model = invocation.getArgument(1);
            model.put("readingChallenge", readingChallenge);

            return null;
        }).when(profileHelper).putReadingChallengeInModel(eq(login), any(ModelMap.class));

        mockMvc.perform(get("/profile/" + login.getHandle()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("login", "totalBooks", "readingProgressList",
                        "connectionsCount", "connections", "feedDtoList", "defaultShelves", "shelves", "activeShelf",
                        "ownProfile", "friendReqSent", "friendReqReceived", "isFriends", "showFriendReqBtn", "readingChallenge"))
                .andExpect(view().name("app/profile/profile"));

        verify(loginService).getLoginByHandle("test");
        verify(profileHelper).setupReferenceData(eq(login.getEmail()), any(LoginDetails.class), any(ModelMap.class));
        verify(profileHelper).putReadingChallengeInModel(eq(login), any(ModelMap.class));
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void showShelfFragment_ownShelf_returnsShelfFragment() throws Exception {
        mockMvc.perform(get("/profile/shelf")
                        .param("shelfId", shelf.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeShelf", shelf))
                .andExpect(view().name("app/profile/profileActiveShelfFragment :: activeShelfFragment"));

        verify(loginService).getLogin(login.getEmail());
    }

    @Test
    void showShelfFragment_unownedShelf_throwsException() {
        assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/profile/shelf")
                        .param("shelfId", otherShelf.getId().toString())));

        verify(loginService).getLogin(login.getEmail());
    }

    @Test
    void showOtherUserShelfFragment_validHandle_returnsShelfFragment() throws Exception {
        when(loginService.getLoginByHandle(otherLogin.getHandle())).thenReturn(otherLogin);

        mockMvc.perform(get("/profile/" + otherLogin.getHandle() + "/shelf")
                        .param("shelfId", otherShelf.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeShelf", otherShelf))
                .andExpect(view().name("app/profile/profileActiveShelfFragment :: activeShelfFragment"));

        verify(loginService).getLoginByHandle(otherLogin.getHandle());
    }

    @Test
    void performFriendAction_sendRequest_callsConnectionActionAndReturnsFragment() throws Exception {
        when(loginService.getLoginByHandle(otherLogin.getHandle())).thenReturn(otherLogin);

        doAnswer(invocation -> {
            ModelMap model = invocation.getArgument(0);
            model.put("ownProfile", true);
            model.put("friendReqSent", true);
            model.put("friendReqReceived", false);
            model.put("isFriends", false);
            model.put("showFriendReqBtn", false);

            return null;
        }).when(profileHelper).setupConnectionRefData(any(ModelMap.class),
                eq(otherLogin), eq(login));

        mockMvc.perform(post("/profile/friendRequest")
                        .param("handle", otherLogin.getHandle())
                        .param("action", ConnectionAction.SEND_FRIEND_REQ.name()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("ownProfile", "friendReqSent", "friendReqReceived", "isFriends", "showFriendReqBtn"))
                .andExpect(view().name("app/profile/profileInfoFragment :: profileInfoFragment"));

        verify(loginService).getLogin(login.getEmail());
        verify(loginService).getLoginByHandle(otherLogin.getHandle());

        verify(profileService).performConnectionAction(login, otherLogin, ConnectionAction.SEND_FRIEND_REQ);
        verify(profileHelper).setupConnectionRefData(any(ModelMap.class), eq(otherLogin), eq(login));
    }

    @Test
    void performFriendAction_acceptRequest_returnsProfileView() throws Exception {
        when(loginService.getLoginByHandle(otherLogin.getHandle())).thenReturn(otherLogin);

        doAnswer(invocation -> {
            populateReferenceData(invocation.getArgument(2), otherLogin, false);

            return null;
        }).when(profileHelper).setupReferenceData(anyString(), any(LoginDetails.class), any(ModelMap.class));

        mockMvc.perform(post("/profile/friendRequest")
                        .param("handle", otherLogin.getHandle())
                        .param("action", ConnectionAction.ACCEPT_FRIEND_REQ.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile/profile"));

        verify(loginService).getLogin(login.getEmail());
        verify(loginService).getLoginByHandle(otherLogin.getHandle());

        verify(profileService).performConnectionAction(login, otherLogin, ConnectionAction.ACCEPT_FRIEND_REQ);
        verify(profileHelper).setupReferenceData(eq(otherLogin.getEmail()), any(LoginDetails.class), any(ModelMap.class));
        verify(profileHelper, never()).setupConnectionRefData(any(ModelMap.class), any(Login.class), any(Login.class));
    }

    @Test
    void performFriendAction_selfAction_throwsException() {
        assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/profile/friendRequest")
                        .param("handle", login.getHandle())
                        .param("action", ConnectionAction.SEND_FRIEND_REQ.name())));

        verify(loginService).getLogin(login.getEmail());
        verify(loginService).getLoginByHandle(login.getHandle());

        verify(profileService, never()).performConnectionAction(login, login, ConnectionAction.SEND_FRIEND_REQ);
        verify(profileHelper, never()).setupConnectionRefData(any(ModelMap.class), eq(login), eq(login));
    }

    @Test
    void showProfile_otherUserProfile_doesNotPopulateReadingChallenge() throws Exception {
        when(loginService.getLoginByHandle(otherLogin.getHandle())).thenReturn(otherLogin);

        doAnswer(invocation -> {
            populateReferenceData(invocation.getArgument(2), otherLogin, false);

            return null;
        }).when(profileHelper).setupReferenceData(anyString(), any(LoginDetails.class), any(ModelMap.class));

        mockMvc.perform(get("/profile/" + otherLogin.getHandle()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("readingChallenge"))
                .andExpect(view().name("app/profile/profile"));

        verify(profileHelper).setupReferenceData(eq(otherLogin.getEmail()), any(LoginDetails.class), any(ModelMap.class));
        verify(profileHelper, never()).putReadingChallengeInModel(any(Login.class), any(ModelMap.class));
        assertThat(applicationEvents.stream(ActivityEvent.class)).hasSize(1);
    }

    @Test
    void saveReadingChallenge_validInput_savesChallengeAndRedirects() throws Exception {
        mockMvc.perform(post("/profile/readingChallenge")
                        .param("year", "2026")
                        .param("bookCount", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/test"));

        verify(loginService).getLogin(login.getEmail());
        verify(readingChallengeRepository).save(any(ReadingChallenge.class));
    }

    @Test
    void saveReadingChallenge_validationErrors_returnsProfileView() throws Exception {
        doAnswer(invocation -> {
            populateReferenceData(invocation.getArgument(2), login, true);

            return null;
        }).when(profileHelper).setupReferenceData(anyString(), any(LoginDetails.class), any(ModelMap.class));

        mockMvc.perform(post("/profile/readingChallenge")
                        .param("year", "2026")
                        .param("bookCount", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile/profile"));

        verify(profileHelper).setupReferenceData(eq(login.getEmail()), any(LoginDetails.class), any(ModelMap.class));
        verify(readingChallengeRepository, never()).save(any(ReadingChallenge.class));
    }

    private void populateReferenceData(ModelMap model, Login profileLogin, boolean ownProfile) {
        model.put("login", profileLogin);
        model.put("totalBooks", 0);
        model.put("readingProgressList", List.of());
        model.put("connectionsCount", 0);
        model.put("connections", List.of());
        model.put("feedDtoList", List.of());
        model.put("defaultShelves", new LinkedHashMap<>());
        model.put("shelves", new LinkedHashMap<>());
        model.put("activeShelf", TestUtils.getShelf(profileLogin, "Read", true));
        model.put("ownProfile", ownProfile);
        model.put("friendReqSent", false);
        model.put("friendReqReceived", false);
        model.put("isFriends", false);
        model.put("showFriendReqBtn", false);
    }
}
