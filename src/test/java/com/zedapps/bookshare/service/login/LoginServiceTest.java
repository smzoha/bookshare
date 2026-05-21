package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import com.zedapps.bookshare.util.Utils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 16/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ActivityService activityService;

    private Login login;

    @BeforeEach
    public void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);
        login.setShelves(new HashSet<>(Utils.getDefaultShelves(login)));

        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        lenient().when(loginRepository.save(any(Login.class))).thenReturn(login);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("password");
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getLogin_activeUserExists_returnsLogin() {
        when(loginRepository.findActiveLoginByEmail(login.getEmail())).thenReturn(Optional.of(login));

        Login activeLogin = loginService.getLogin(login.getEmail());

        assertNotNull(activeLogin);
        assertEquals(login.getEmail(), activeLogin.getEmail());
        assertEquals(login.isActive(), activeLogin.isActive());
    }

    @Test
    void getLogin_userNotFound_throwsNoResultException() {
        when(loginRepository.findActiveLoginByEmail(login.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> loginService.getLogin(login.getEmail()));
    }

    @Test
    void getLoginByHandle_existingHandle_returnsLogin() {
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.of(login));

        Login loginByHandle = loginService.getLoginByHandle(login.getHandle());

        assertNotNull(loginByHandle);
        assertEquals(login.getHandle(), loginByHandle.getHandle());
    }

    @Test
    void getLoginByHandle_missingHandle_throwsNoResultException() {
        when(loginRepository.findByHandle(login.getHandle())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> loginService.getLoginByHandle(login.getHandle()));
    }

    @Test
    void createLogin_newRegistration_encodesPassword() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);

        loginService.createLogin(registrationRequestDto);

        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void createLogin_newRegistration_setsRoleUserAndActiveTrue() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);

        Login persistedLogin = loginService.createLogin(registrationRequestDto);

        assertEquals(Role.USER, persistedLogin.getRole());
        assertTrue(persistedLogin.isActive());
    }

    @Test
    void createLogin_newRegistration_createsThreeDefaultShelves() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);

        Login persistedLogin = loginService.createLogin(registrationRequestDto);

        assertFalse(persistedLogin.getShelves().isEmpty());
        assertEquals(3, persistedLogin.getShelves().size());
    }

    @Test
    void createLogin_newRegistration_firesRegisterOutbox() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);

        Login persistedLogin = loginService.createLogin(registrationRequestDto);

        verify(activityService).saveActivityOutbox(eq(ActivityType.REGISTER),
                eq(persistedLogin.getId()), anyMap());
    }

    @Test
    void saveLogin_dto_newLogin_setsUpDefaultShelves() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        when(loginRepository.findByEmail(loginManageDto.getEmail())).thenReturn(Optional.empty());

        loginService.saveLogin(loginManageDto);

        ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);
        verify(loginRepository).save(loginCaptor.capture());

        assertFalse(loginCaptor.getValue().getShelves().isEmpty());
        assertEquals(3, loginCaptor.getValue().getShelves().size());
    }

    @Test
    void saveLogin_dto_existingLoginById_updatesFieldsWithoutCreatingShelves() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        loginManageDto.setId(login.getId());

        when(loginRepository.findById(login.getId())).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        loginService.saveLogin(loginManageDto);

        ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);
        verify(loginRepository).save(loginCaptor.capture());

        assertEquals(3, loginCaptor.getValue().getShelves().size());
    }

    @Test
    void saveLogin_dto_passwordChangedFromDto_encodesNewPassword() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        loginManageDto.setId(login.getId());

        when(loginRepository.findById(login.getId())).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(loginManageDto.getPassword(), login.getPassword())).thenReturn(false);

        loginService.saveLogin(loginManageDto);

        verify(passwordEncoder).encode(loginManageDto.getPassword());
    }

    @Test
    void saveLogin_dto_passwordUnchanged_doesNotReEncode() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        loginManageDto.setId(login.getId());

        when(loginRepository.findById(login.getId())).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(loginManageDto.getPassword(), login.getPassword())).thenReturn(true);

        loginService.saveLogin(loginManageDto);

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void saveLogin_dto_newLogin_firesUserAddOutbox() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        when(loginRepository.findByEmail(loginManageDto.getEmail())).thenReturn(Optional.empty());

        loginService.saveLogin(loginManageDto);

        verify(activityService).saveActivityOutbox(eq(ActivityType.USER_ADD),
                eq(login.getId()), anyMap());
    }

    @Test
    void saveLogin_dto_existingLogin_firesUserUpdateOutbox() {
        LoginManageDto loginManageDto = TestUtils.getLoginManageDto(login);
        loginManageDto.setId(login.getId());

        when(loginRepository.findById(login.getId())).thenReturn(Optional.of(login));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        loginService.saveLogin(loginManageDto);

        verify(activityService).saveActivityOutbox(eq(ActivityType.USER_UPDATE),
                eq(login.getId()), anyMap());
    }
}
