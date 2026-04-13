package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph("login.withCollections")
    @Query("FROM Login WHERE email = :email AND active = TRUE")
    Optional<Login> findActiveLoginByEmail(String email);

    @EntityGraph("login.withCollections")
    Optional<Login> findByEmail(String email);

    @EntityGraph("login.withCollections")
    Optional<Login> findByHandle(String handle);

    List<Login> findAllByActive(boolean active);

    List<Login> findAllByRoleAndActive(Role role, boolean active);
}
