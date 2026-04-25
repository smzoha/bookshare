package com.zedapps.bookshare.repository;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.AuthProvider;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.login.LoginRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 25/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LoginRepositoryTest {

    private final String ACTIVE_EMAIL = "active@test.com";
    private final String ACTIVE_HANDLE = "active";
    private final String INACTIVE_EMAIL = "inactive@test.com";
    private final String INVALID_EMAIL = "invalid@invalid.invalid";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private LoginRepository loginRepository;

    @BeforeEach
    void setupLogins() {
        saveLoginsForTest();
    }

    @Test
    void findActiveLoginByEmail_returnActiveLogin() {
        Optional<Login> activeLoginOptional = loginRepository.findActiveLoginByEmail(ACTIVE_EMAIL);

        assertTrue(activeLoginOptional.isPresent());
        assertEquals(ACTIVE_EMAIL, activeLoginOptional.get().getEmail());
        assertTrue(activeLoginOptional.get().isActive());
    }

    @Test
    void findActiveLoginByEmail_returnEmptyForInactiveLogin() {
        Optional<Login> inactiveLoginOptionalEmpty = loginRepository.findActiveLoginByEmail(INACTIVE_EMAIL);
        assertTrue(inactiveLoginOptionalEmpty.isEmpty());

        Optional<Login> inactiveLoginOptional = loginRepository.findByEmail(INACTIVE_EMAIL);
        assertTrue(inactiveLoginOptional.isPresent());
        assertEquals(INACTIVE_EMAIL, inactiveLoginOptional.get().getEmail());
        assertFalse(inactiveLoginOptional.get().isActive());
    }

    @Test
    void findLoginByEmail_returnLogin() {
        Optional<Login> activeLoginOptional = loginRepository.findByEmail(ACTIVE_EMAIL);
        assertTrue(activeLoginOptional.isPresent());
        assertEquals(ACTIVE_EMAIL, activeLoginOptional.get().getEmail());
        assertTrue(activeLoginOptional.get().isActive());

        Optional<Login> inactiveLoginOptional = loginRepository.findByEmail(INACTIVE_EMAIL);
        assertTrue(inactiveLoginOptional.isPresent());
        assertEquals(INACTIVE_EMAIL, inactiveLoginOptional.get().getEmail());
        assertFalse(inactiveLoginOptional.get().isActive());

        Optional<Login> invalidLoginOptional = loginRepository.findByEmail(INVALID_EMAIL);
        assertTrue(invalidLoginOptional.isEmpty());
    }

    @Test
    void findLoginByHandle_returnLogin() {
        Optional<Login> activeLoginOptional = loginRepository.findByHandle(ACTIVE_HANDLE);
        assertTrue(activeLoginOptional.isPresent());
        assertEquals(ACTIVE_HANDLE, activeLoginOptional.get().getHandle());

        Optional<Login> invalidLoginOptional = loginRepository.findByHandle("invalid");
        assertTrue(invalidLoginOptional.isEmpty());
    }

    @Test
    void findAllByRoleAndActive_returnLoginList() {
        List<Login> activeLoginWithUserRole = loginRepository.findAllByRoleAndActive(Role.USER, true);
        assertEquals(1, activeLoginWithUserRole.size());
        assertEquals(Role.USER, activeLoginWithUserRole.getFirst().getRole());
        assertTrue(activeLoginWithUserRole.getFirst().isActive());

        List<Login> inactiveLoginWithUserRole = loginRepository.findAllByRoleAndActive(Role.USER, false);
        assertEquals(1, inactiveLoginWithUserRole.size());
        assertEquals(Role.USER, inactiveLoginWithUserRole.getFirst().getRole());
        assertFalse(inactiveLoginWithUserRole.getFirst().isActive());

        List<Login> activeLoginWithAdminRole = loginRepository.findAllByRoleAndActive(Role.ADMIN, true);
        assertTrue(activeLoginWithAdminRole.isEmpty());
    }

    @Test
    void existsByLoginEmail_returnBoolean() {
        boolean activeLoginExists = loginRepository.existsLoginByEmail(ACTIVE_EMAIL);
        assertTrue(activeLoginExists);

        boolean inactiveLoginExists = loginRepository.existsLoginByEmail(INACTIVE_EMAIL);
        assertTrue(inactiveLoginExists);

        boolean invalidLoginExists = loginRepository.existsLoginByEmail(INVALID_EMAIL);
        assertFalse(invalidLoginExists);
    }

    private void saveLoginsForTest() {
        Login activeLogin = getLogin(ACTIVE_EMAIL, ACTIVE_HANDLE, true);
        Login inactiveLogin = getLogin(INACTIVE_EMAIL, "inactive", false);

        loginRepository.saveAllAndFlush(List.of(activeLogin, inactiveLogin));
    }

    private static Login getLogin(String email, String handle, boolean active) {
        Login login = new Login();
        login.setEmail(email);
        login.setHandle(handle);
        login.setRole(Role.USER);
        login.setFirstName("Test");
        login.setLastName("User");
        login.setActive(active);
        login.setAuthProvider(AuthProvider.LOCAL);

        return login;
    }
}
