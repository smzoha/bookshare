package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.PasswordResetToken;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.exception.MailSendException;
import com.zedapps.bookshare.exception.TokenGenerationException;
import com.zedapps.bookshare.repository.login.PasswordResetTokenRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.mail.MailService;
import com.zedapps.bookshare.util.TestUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 17/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private ActivityService activityService;

    @Mock
    private MailService mailService;

    @Mock
    private LoginService loginService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Errors errors;

    private Login login;
    private PasswordResetToken passwordResetToken;
    private PasswordResetDto passwordResetDto;

    @BeforeEach
    void setup() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        passwordResetToken = new PasswordResetToken(1L, login.getEmail(), "token",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), false);

        lenient().when(loginService.getLogin(login.getEmail())).thenReturn(login);
        lenient().when(loginService.saveLogin(login)).thenReturn(login);
        lenient().when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        lenient().when(passwordResetTokenRepository.getPasswordResetTokenByHashedSignature(anyString()))
                .thenReturn(Optional.of(passwordResetToken));

        passwordResetDto = new PasswordResetDto("token");
        passwordResetDto.setPassword("password");
        passwordResetDto.setConfirmPassword("password");
    }

    @Test
    void savePasswordResetToken_validEmail_generatesAndSavesHashedToken() {
        passwordResetService.savePasswordResetToken(login.getEmail());

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void savePasswordResetToken_validEmail_setsExpiryTenMinutesFromNow() {
        passwordResetService.savePasswordResetToken(login.getEmail());

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());

        LocalDateTime expiryTimestamp = captor.getValue().getExpiryTimestamp();
        LocalDateTime expectedValue = LocalDateTime.now().plusMinutes(10);

        assertTrue(Duration.between(expectedValue, expiryTimestamp).abs().toSeconds() < 5);
    }

    @Test
    void savePasswordResetToken_validEmail_sendsPasswordResetEmail() throws MessagingException, IOException {
        passwordResetService.savePasswordResetToken(login.getEmail());

        verify(mailService).sendPasswordResetEmail(eq(login.getEmail()), anyString());
    }

    @Test
    void savePasswordResetToken_validEmail_firesActivityOutbox() {
        passwordResetService.savePasswordResetToken(login.getEmail());

        verify(activityService).saveActivityOutbox(eq(ActivityType.RESET_PASSWORD_REQUEST),
                eq(login.getId()), anyMap());
    }

    @Test
    void savePasswordResetToken_mailSendFailure_throwsMailSendException() throws MessagingException, IOException {
        doThrow(MailSendException.class).when(mailService).sendPasswordResetEmail(eq(login.getEmail()), anyString());

        assertThrows(MailSendException.class, () -> passwordResetService.savePasswordResetToken(login.getEmail()));
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void savePasswordResetToken_hashingFailure_throwsTokenGenerationException() {
        try (MockedStatic<MessageDigest> mockedDigest = Mockito.mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(NoSuchAlgorithmException.class);

            assertThrows(TokenGenerationException.class,
                    () -> passwordResetService.savePasswordResetToken(login.getEmail()));
        }
    }

    @Test
    void validateToken_validNonExpiredToken_doesNotThrow() {
        assertDoesNotThrow(() -> passwordResetService.validateToken("token"));
    }

    @Test
    void validateToken_expiredToken_throwsIllegalArgumentException() {
        passwordResetToken.setExpiryTimestamp(LocalDateTime.now().minusMinutes(10));

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.validateToken("token"));
    }

    @Test
    void validateToken_inactiveToken_throwsIllegalArgumentException() {
        passwordResetToken.setInactive(true);

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.validateToken("token"));
    }

    @Test
    void validateToken_unknownToken_throwsNoResultException() {
        when(passwordResetTokenRepository.getPasswordResetTokenByHashedSignature(anyString())).thenReturn(Optional.empty());

        assertThrows(NoResultException.class, () -> passwordResetService.validateToken("token"));
    }

    @Test
    void validatePasswordResetDto_passwordMismatch_rejectsConfirmPassword() {
        passwordResetDto.setConfirmPassword("password1");

        passwordResetService.validatePasswordResetDto(passwordResetDto, errors);

        verify(errors).rejectValue("password", "error.password.do.not.match");
    }

    @Test
    void validatePasswordResetDto_invalidToken_populatesErrors() {
        when(passwordResetTokenRepository.getPasswordResetTokenByHashedSignature(anyString()))
                .thenReturn(Optional.empty());

        passwordResetService.validatePasswordResetDto(new PasswordResetDto("invalidToken"), errors);
        verify(errors).reject("error.invalid");
    }

    @Test
    void resetPassword_validRequest_encodesAndSavesNewPassword() {
        passwordResetService.resetPassword(passwordResetDto);

        verify(passwordEncoder).encode(passwordResetDto.getPassword());
        assertEquals("encoded-password", login.getPassword());
    }

    @Test
    void resetPassword_validRequest_marksTokenInactive() {
        passwordResetService.resetPassword(passwordResetDto);

        assertTrue(passwordResetToken.isInactive());
    }

    @Test
    void resetPassword_validRequest_firesResetPasswordOutbox() {
        passwordResetService.resetPassword(passwordResetDto);

        verify(activityService).saveActivityOutbox(eq(ActivityType.RESET_PASSWORD),
                eq(login.getId()), anyMap());
    }
}
