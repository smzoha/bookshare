package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.PasswordResetToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 26/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PasswordResetTokenRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void getPasswordResetTokenByHashedSignature_returnToken() {
        setupPasswordResetToken();

        Optional<PasswordResetToken> token = passwordResetTokenRepository
                .getPasswordResetTokenByHashedSignature("valid-signature");

        assertTrue(token.isPresent());
        assertEquals("test@test.com", token.get().getEmail());
        assertEquals("valid-signature", token.get().getHashedSignature());
        assertTrue(token.get().getExpiryTimestamp().isAfter(LocalDateTime.now()));
        assertFalse(token.get().isInactive());

        Optional<PasswordResetToken> invalidToken = passwordResetTokenRepository
                .getPasswordResetTokenByHashedSignature("invalid-signature");

        assertTrue(invalidToken.isEmpty());
    }

    private void setupPasswordResetToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("test@test.com");
        token.setHashedSignature("valid-signature");
        token.setInactive(false);
        token.setGeneratedAt(LocalDateTime.now());
        token.setExpiryTimestamp(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.saveAndFlush(token);
    }
}
