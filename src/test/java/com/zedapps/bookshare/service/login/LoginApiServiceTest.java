package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import com.zedapps.bookshare.validator.LoginDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 16/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class LoginApiServiceTest {

    @InjectMocks
    private LoginApiService loginApiService;

    @Mock
    private LoginService loginService;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private LoginDtoValidator loginDtoValidator;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private Errors errors;

    private Login login;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
    }

    @Test
    void registerLogin_validRequest_returnsLoginApiDto() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);
        when(loginService.createLogin(registrationRequestDto)).thenReturn(login);

        LoginApiDto loginApiDto = loginApiService.registerLogin(registrationRequestDto);

        assertEquals(login.getEmail(), loginApiDto.email());
        assertEquals(login.getHandle(), loginApiDto.handle());
        assertEquals(login.getFirstName(), loginApiDto.firstName());
        assertEquals(login.getLastName(), loginApiDto.lastName());
        assertEquals(login.isActive(), loginApiDto.active());
    }

    @Test
    void saveResetPasswordToken_knownEmail_savesTokenAndReturnsTrue() {
        when(loginRepository.existsLoginByEmail(login.getEmail())).thenReturn(true);

        boolean passwordResetTokenSent = loginApiService.saveResetPasswordToken(login.getEmail());

        assertTrue(passwordResetTokenSent);
        verify(passwordResetService).savePasswordResetToken(eq(login.getEmail()));
    }

    @Test
    void saveResetPasswordToken_unknownEmail_returnsFalse() {
        when(loginRepository.existsLoginByEmail(login.getEmail())).thenReturn(false);

        boolean passwordResetTokenSent = loginApiService.saveResetPasswordToken(login.getEmail());

        assertFalse(passwordResetTokenSent);
        verify(passwordResetService, never()).savePasswordResetToken(eq(login.getEmail()));
    }

    @Test
    void resetPassword_validDto_resetsAndReturnsTrue() {
        PasswordResetDto resetDto = new PasswordResetDto("test-token-for-password-reset");

        boolean passwordReset = loginApiService.resetPassword(resetDto, errors);

        assertTrue(passwordReset);
    }

    @Test
    void resetPassword_invalidDto_returnsFalseWithoutResetting() {
        PasswordResetDto resetDto = new PasswordResetDto();
        when(errors.hasErrors()).thenReturn(true);

        boolean passwordReset = loginApiService.resetPassword(resetDto, errors);

        assertFalse(passwordReset);
    }

    @Test
    void validateRegistration_delegatesToLoginDtoValidator() {
        RegistrationRequestDto registrationRequestDto = TestUtils.getRegistrationRequestDto(login);
        loginApiService.validateRegistration(registrationRequestDto, errors);

        verify(loginDtoValidator).validate(registrationRequestDto, errors);
    }
}
