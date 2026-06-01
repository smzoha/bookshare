package com.zedapps.bookshare.service.auth;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author smzoha
 * @since 4/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class LoginDetailServiceTest {

    @InjectMocks
    private LoginDetailService loginDetailService;

    @Mock
    private LoginRepository loginRepository;

    private Login activeLogin;

    @BeforeEach
    void setUp() {
        activeLogin = TestUtils.getLogin("test@test.com", "test", true);
        activeLogin.setPassword("very-good-password");
    }

    @Test
    void loadUserByUsername_activeUserExists_returnsLoginDetails() {
        Mockito.when(loginRepository.findActiveLoginByEmail(any())).thenReturn(Optional.of(activeLogin));

        LoginDetails loginDetails = (LoginDetails) loginDetailService.loadUserByUsername("test@test.com");

        assertEquals("test@test.com", loginDetails.getEmail());
        assertEquals("test@test.com", loginDetails.getUsername());
        assertEquals("test", loginDetails.getHandle());
        assertEquals("very-good-password", loginDetails.getPassword());

        assertEquals("Test", loginDetails.getFirstName());
        assertEquals("User", loginDetails.getLastName());

        assertEquals(1, loginDetails.getAuthorities().size());
        assertTrue(loginDetails.getAuthorities().stream().allMatch(authority -> authority.getAuthority().equals(Role.USER.name())));
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        Mockito.when(loginRepository.findActiveLoginByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> loginDetailService.loadUserByUsername("invalid@test.com"));
    }
}
