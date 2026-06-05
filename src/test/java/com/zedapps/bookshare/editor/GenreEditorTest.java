package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.repository.book.GenreRepository;
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
public class GenreEditorTest {

    @InjectMocks
    private GenreEditor genreEditor;

    @Mock
    private GenreRepository genreRepository;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = TestUtils.getGenre("Genre");
        genre.setId(1L);
    }

    @Test
    void setAsText_nullString() {
        genreEditor.setAsText(null);
        assertNull(genreEditor.getAsText());
    }

    @Test
    void setAsText_emptyString() {
        genreEditor.setAsText("  ");
        assertNull(genreEditor.getAsText());
    }

    @Test
    void setAsText_validId() {
        when(genreRepository.findById(genre.getId())).thenReturn(Optional.of(genre));

        genreEditor.setAsText("1");
        assertEquals("1", genreEditor.getAsText());

        verify(genreRepository).findById(genre.getId());
    }

    @Test
    void setAsText_invalidId() {
        when(genreRepository.findById(genre.getId())).thenReturn(Optional.empty());

        genreEditor.setAsText("1");
        assertNull(genreEditor.getAsText());

        verify(genreRepository).findById(genre.getId());
    }

    @Test
    void getAsText_nullGenre() {
        genreEditor.setValue(null);
        assertNull(genreEditor.getAsText());
    }

    @Test
    void getAsText_validGenre() {
        genreEditor.setValue(genre);
        assertEquals("1", genreEditor.getAsText());
    }
}
