package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.PasswordResetDto;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.PasswordResetToken;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.exception.MailSendException;
import com.zedapps.bookshare.exception.TokenGenerationException;
import com.zedapps.bookshare.repository.login.PasswordResetTokenRepository;
import com.zedapps.bookshare.service.mail.MailService;
import com.zedapps.bookshare.service.activity.ActivityService;
import jakarta.mail.MessagingException;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ActivityService activityService;
    private final MailService mailService;
    private final LoginService loginService;
    private final PasswordEncoder passwordEncoder;

    private static final long EXPIRY_OFFSET_MINS = 10;

    public void savePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        try {
            PasswordResetToken resetToken = getPasswordResetToken(email, token, now);

            mailService.sendPasswordResetEmail(email, token);
            passwordResetTokenRepository.save(resetToken);

            Login login = loginService.getLogin(email);

            activityService.saveActivityOutbox(ActivityType.RESET_PASSWORD_REQUEST,
                    login.getId(),
                    Map.of(
                            "affectedUserEmail", login.getEmail()
                    ));

        } catch (NoSuchAlgorithmException e) {
            log.error("Error while generating hashed token", e);
            throw new TokenGenerationException("Error while generating hashed token", e);

        } catch (MessagingException | IOException e) {
            log.error("Error while sending mail", e);
            throw new MailSendException("Error while sending mail", e);
        }
    }

    public void validateToken(String token) {
        String signature = getHashedSignatureFromToken(token);

        PasswordResetToken resetToken = getPasswordResetToken(signature);

        if (resetToken.isInactive() || resetToken.getExpiryTimestamp().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token is expired!");
        }
    }

    public void validatePasswordResetDto(PasswordResetDto resetDto, Errors errors) {
        validateToken(resetDto.getToken());

        if (!Objects.equals(resetDto.getPassword(), resetDto.getConfirmPassword())) {
            errors.rejectValue("password", "error.password.do.not.match");
        }
    }

    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        String token = passwordResetDto.getToken();
        String signature = getHashedSignatureFromToken(token);

        PasswordResetToken resetToken = getPasswordResetToken(signature);

        Login login = loginService.getLogin(resetToken.getEmail());
        login.setPassword(passwordEncoder.encode(passwordResetDto.getPassword()));
        login = loginService.saveLogin(login);

        activityService.saveActivityOutbox(ActivityType.RESET_PASSWORD,
                login.getId(),
                Map.of(
                        "affectedUserEmail", login.getEmail()
                ));

        resetToken.setInactive(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private PasswordResetToken getPasswordResetToken(String signature) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.getPasswordResetTokenByHashedSignature(signature);

        if (resetToken.isEmpty()) {
            throw new NoResultException("Password Reset Token not found!");
        }

        return resetToken.get();
    }

    private PasswordResetToken getPasswordResetToken(String email, String token,
                                                     LocalDateTime now) throws NoSuchAlgorithmException {

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setEmail(email);
        resetToken.setHashedSignature(getHashedSignature(token));
        resetToken.setGeneratedAt(now);
        resetToken.setExpiryTimestamp(now.plusMinutes(EXPIRY_OFFSET_MINS));

        return resetToken;
    }

    private String getHashedSignatureFromToken(String token) {
        String signature;

        try {
            signature = getHashedSignature(token);

        } catch (NoSuchAlgorithmException e) {
            log.error("Error while generating hashed token", e);
            throw new TokenGenerationException("Error while generating hashed token", e);
        }

        return signature;
    }

    private String getHashedSignature(String token) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hashedToken = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hashedToken);
    }
}
