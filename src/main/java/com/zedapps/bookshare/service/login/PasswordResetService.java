package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.entity.login.PasswordResetToken;
import com.zedapps.bookshare.exception.MailSendException;
import com.zedapps.bookshare.exception.TokenGenerationException;
import com.zedapps.bookshare.repository.login.PasswordResetTokenRepository;
import com.zedapps.bookshare.service.MailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    private static final long EXPIRY_OFFSET_MINS = 10;

    public void savePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        try {
            PasswordResetToken resetToken = getPasswordResetToken(email, token, now);

            mailService.sendPasswordResetEmail(email, token);
            passwordResetTokenRepository.save(resetToken);

        } catch (NoSuchAlgorithmException e) {
            log.error("Error while generating hashed token", e);
            throw new TokenGenerationException("Error while generating hashed token", e);

        } catch (MessagingException | IOException e) {
            log.error("Error while sending mail", e);
            throw new MailSendException("Error while sending mail", e);
        }
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

    private String getHashedSignature(String token) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hashedToken = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hashedToken);
    }
}
