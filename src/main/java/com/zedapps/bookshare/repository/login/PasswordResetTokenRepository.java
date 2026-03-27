package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author smzoha
 * @since 27/3/26
 **/
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> getPasswordResetTokenByHashedSignature(String hashedSignature);
}
