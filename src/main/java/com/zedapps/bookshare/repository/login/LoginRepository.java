package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author smzoha
 * @since 11/9/25
 **/
@Repository
public interface LoginRepository extends JpaRepository<Login, Long> {

    @Query("FROM Login WHERE email = :email AND active = TRUE")
    Optional<Login> findActiveLoginByEmail(String email);

    Optional<Login> findByEmail(String email);

    Optional<Login> findByHandle(String handle);

    List<Login> findAllByActive(boolean active);
}
