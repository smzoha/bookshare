package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.repository.book.TagRepository;
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
public class TagEditorTest {

    @InjectMocks
    private TagEditor tagEditor;

    @Mock
    private TagRepository tagRepository;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = TestUtils.getTag("Tag");
        tag.setId(1L);
    }

    @Test
    void setAsText_nullString() {
        tagEditor.setAsText(null);
        assertNull(tagEditor.getAsText());
    }

    @Test
    void setAsText_emptyString() {
        tagEditor.setAsText("  ");
        assertNull(tagEditor.getAsText());
    }

    @Test
    void setAsText_validId() {
        when(tagRepository.findById(tag.getId())).thenReturn(Optional.of(tag));

        tagEditor.setAsText("1");
        assertEquals("1", tagEditor.getAsText());

        verify(tagRepository).findById(tag.getId());
    }

    @Test
    void setAsText_invalidId() {
        when(tagRepository.findById(tag.getId())).thenReturn(Optional.empty());

        tagEditor.setAsText("1");
        assertNull(tagEditor.getAsText());

        verify(tagRepository).findById(tag.getId());
    }

    @Test
    void getAsText_nullTag() {
        tagEditor.setValue(null);
        assertNull(tagEditor.getAsText());
    }

    @Test
    void getAsText_validTag() {
        tagEditor.setValue(tag);
        assertEquals("1", tagEditor.getAsText());
    }
}
