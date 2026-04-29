package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.AuthorRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 26/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthorRequestRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private AuthorRequestRepository authorRequestRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Test
    void getAuthorRequestsByLoginEmail_returnAuthorRequest() {
        setupAuthorRequest();

        Optional<AuthorRequest> request = authorRequestRepository.getAuthorRequestsByLoginEmail("author@test.com");
        assertTrue(request.isPresent());
        assertNotNull(request.get().getLogin());
        assertEquals("author@test.com", request.get().getLogin().getEmail());

        Optional<AuthorRequest> invalidRequest = authorRequestRepository.getAuthorRequestsByLoginEmail("invalid@test.com");
        assertTrue(invalidRequest.isEmpty());
    }

    private void setupAuthorRequest() {
        Login login = TestUtils.getLogin("author@test.com", "author", true);
        loginRepository.saveAndFlush(login);

        AuthorRequest authorRequest = new AuthorRequest();
        authorRequest.setLogin(login);

        authorRequestRepository.saveAndFlush(authorRequest);
    }
}
