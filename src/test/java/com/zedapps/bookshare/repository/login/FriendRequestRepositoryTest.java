package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.FriendRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.util.TestUtils;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author smzoha
 * @since 27/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FriendRequestRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    private Login person1;
    private Login person2;

    @BeforeEach
    void setupConnections() {
        person1 = TestUtils.getLogin("person1@test.com", "person1", true);
        person2 = TestUtils.getLogin("person2@test.com", "person2", true);

        loginRepository.saveAllAndFlush(List.of(person1, person2));

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setPerson1(person1);
        friendRequest.setPerson2(person2);

        friendRequestRepository.saveAndFlush(friendRequest);
    }

    @Test
    void findFriendRequest_returnRequest() {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(person1, person2);

        assertTrue(request.isPresent());
        assertEquals(person1, request.get().getPerson1());
        assertEquals(person2, request.get().getPerson2());

        Optional<FriendRequest> reverseRequest = friendRequestRepository.findFriendRequest(person2, person1);
        assertTrue(reverseRequest.isEmpty());
    }
}
