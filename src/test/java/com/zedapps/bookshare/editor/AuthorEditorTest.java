package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author smzoha
 * @since 5/6/26
 **/
@ExtendWith(MockitoExtension.class)
public class AuthorEditorTest {

    @InjectMocks
    private AuthorEditor authorEditor;

    @Mock
    private AuthorRepository authorRepository;

    private Author author;

    @BeforeEach
    void setUp() {
        author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);
    }

    @Test
    void setAsText_nullString() {
        authorEditor.setAsText(null);
        assertNull(authorEditor.getAsText());
    }

    @Test
    void setAsText_emptyString() {
        authorEditor.setAsText("  ");
        assertNull(authorEditor.getAsText());
    }

    @Test
    void setAsText_validId() {
        when(authorRepository.findById(author.getId())).thenReturn(Optional.of(author));

        authorEditor.setAsText("1");
        assertEquals("1", authorEditor.getAsText());

        verify(authorRepository).findById(author.getId());
    }

    @Test
    void setAsText_invalidId() {
        when(authorRepository.findById(author.getId())).thenReturn(Optional.empty());

        authorEditor.setAsText("1");
        assertNull(authorEditor.getAsText());

        verify(authorRepository).findById(author.getId());
    }

    @Test
    void getAsText_nullAuthor() {
        authorEditor.setValue(null);
        assertNull(authorEditor.getAsText());
    }

    @Test
    void getAsText_validAuthor() {
        authorEditor.setValue(author);
        assertEquals("1", authorEditor.getAsText());
    }
}
