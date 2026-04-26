package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Connection;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author smzoha
 * @since 27/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ConnectionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private LoginRepository loginRepository;

    private Login person1;
    private Login person2;
    private Login person3;

    @BeforeEach
    void setupConnections() {
        person1 = TestUtils.getLogin("person1@test.com", "person1", true);
        person2 = TestUtils.getLogin("person2@test.com", "person2", true);
        person3 = TestUtils.getLogin("person3@test.com", "person3", true);

        loginRepository.saveAllAndFlush(List.of(person1, person2, person3));

        Connection connection = new Connection(person1, person2);
        connectionRepository.saveAndFlush(connection);
    }

    @Test
    void findConnectionsByPerson1_returnConnectionList() {
        List<Connection> connectionList = connectionRepository.findConnectionsByPerson1(person1);

        assertFalse(connectionList.isEmpty());
        assertEquals(1, connectionList.size());
        assertEquals(connectionList.getFirst().getPerson2(), person2);

        List<Connection> emptyConnectionList = connectionRepository.findConnectionsByPerson1(person3);
        assertTrue(emptyConnectionList.isEmpty());
    }

    @Test
    void findConnectionByPerson1AndPerson2_returnConnection() {
        Connection connection = connectionRepository.findConnectionByPerson1AndPerson2(person1, person2);

        assertNotNull(connection);
        assertEquals(person1, connection.getPerson1());
        assertEquals(person2, connection.getPerson2());

        Connection emptyConnection = connectionRepository.findConnectionByPerson1AndPerson2(person1, person3);
        assertNull(emptyConnection);
    }
}
