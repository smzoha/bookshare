package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Tag;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author smzoha
 * @since 26/4/26
 **/
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TagRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TagRepository tagRepository;

    @Test
    void findTagByName_returnTag() {
        setupTags();

        Optional<Tag> persistedTag = tagRepository.findTagByName("Test Tag");
        assertTrue(persistedTag.isPresent());
        assertEquals("Test Tag", persistedTag.get().getName());

        Optional<Tag> invalidTag = tagRepository.findTagByName("Invalid Tag");
        assertTrue(invalidTag.isEmpty());
    }

    private void setupTags() {
        Tag tag = TestUtils.getTag("Test Tag");

        tagRepository.saveAndFlush(tag);
    }
}