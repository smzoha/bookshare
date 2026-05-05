package com.zedapps.bookshare.service.auth;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.AuthProvider;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 5/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class LoginDetailOidcServiceTest {

    @InjectMocks
    private LoginDetailOidcService loginDetailOidcService;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private OidcUserService oidcUserService;

    @Mock
    private OidcUserRequest oidcUserRequest;

    @Mock
    private OidcUser oidcUser;

    @Mock
    private OidcIdToken idToken;

    @Mock
    private OidcUserInfo userInfo;

    private Login activeLogin;
    private Login inactiveLogin;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(loginDetailOidcService, "oidcUserService", oidcUserService);

        activeLogin = TestUtils.getLogin("test@test.com", "test", true);
        inactiveLogin = TestUtils.getLogin("test@test.com", "test", false);

        when(oidcUserService.loadUser(oidcUserRequest)).thenReturn(oidcUser);
        when(oidcUser.getEmail()).thenReturn("test@test.com");
        when(oidcUser.getGivenName()).thenReturn("Test");
        when(oidcUser.getFamilyName()).thenReturn("User");
        when(oidcUser.getSubject()).thenReturn("google-subject-id");

        lenient().when(oidcUser.getIdToken()).thenReturn(idToken);
        lenient().when(oidcUser.getUserInfo()).thenReturn(userInfo);
    }

    @Test
    void loadUser_newUser_createsLoginWithUserRoleAndGoogleProvider() {
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
        when(loginRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        loginDetailOidcService.loadUser(oidcUserRequest);

        Login saved = captor.getValue();
        assertEquals(Role.USER, saved.getRole());
        assertEquals(AuthProvider.GOOGLE, saved.getAuthProvider());
    }

    @Test
    void loadUser_newUser_generatesUniqueHandle() {
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
        when(loginRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        loginDetailOidcService.loadUser(oidcUserRequest);

        String handle = captor.getValue().getHandle();
        assertThat(handle).startsWith("Test.User.");
        assertThat(handle.substring("Test.User.".length())).hasSize(4);
    }

    @Test
    void loadUser_existingActiveUser_returnsExistingLogin() {
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.of(activeLogin));

        LoginDetails result = (LoginDetails) loginDetailOidcService.loadUser(oidcUserRequest);

        assertEquals("test@test.com", result.getEmail());
        assertEquals("test", result.getHandle());
        verify(loginRepository, never()).save(any());
    }

    @Test
    void loadUser_existingInactiveUser_throwsOAuth2AuthenticationException() {
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.of(inactiveLogin));

        assertThrows(OAuth2AuthenticationException.class, () -> loginDetailOidcService.loadUser(oidcUserRequest));
    }

    @Test
    void loadUser_nullFirstName_handlesGracefully() {
        when(oidcUser.getGivenName()).thenReturn(null);
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(loginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginDetails result = assertDoesNotThrow(() -> (LoginDetails) loginDetailOidcService.loadUser(oidcUserRequest));

        assertNull(result.getFirstName());
    }

    @Test
    void loadUser_nullLastName_handlesGracefully() {
        when(oidcUser.getFamilyName()).thenReturn(null);
        when(loginRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(loginRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoginDetails result = assertDoesNotThrow(() -> (LoginDetails) loginDetailOidcService.loadUser(oidcUserRequest));

        assertNull(result.getLastName());
    }
}
