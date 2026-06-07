package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.image.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
public class ImageEditorTest {

    @InjectMocks
    private ImageEditor imageEditor;

    @Mock
    private ImageRepository imageRepository;

    private Image image;

    @BeforeEach
    void setUp() {
        image = new Image();
        image.setId(1L);
        image.setFileName("test.png");
        image.setContentType("image/png");
        image.setUploadDate(LocalDateTime.now());
    }

    @Test
    void setAsText_nullString() {
        imageEditor.setAsText(null);
        assertNull(imageEditor.getAsText());
    }

    @Test
    void setAsText_emptyString() {
        imageEditor.setAsText("  ");
        assertNull(imageEditor.getAsText());
    }

    @Test
    void setAsText_validId() {
        when(imageRepository.findById(image.getId())).thenReturn(Optional.of(image));

        imageEditor.setAsText("1");
        assertEquals("1", imageEditor.getAsText());

        verify(imageRepository).findById(image.getId());
    }

    @Test
    void setAsText_invalidId() {
        when(imageRepository.findById(image.getId())).thenReturn(Optional.empty());

        imageEditor.setAsText("1");
        assertNull(imageEditor.getAsText());

        verify(imageRepository).findById(image.getId());
    }

    @Test
    void getAsText_nullImage() {
        imageEditor.setValue(null);
        assertNull(imageEditor.getAsText());
    }

    @Test
    void getAsText_validImage() {
        imageEditor.setValue(image);
        assertEquals("1", imageEditor.getAsText());
    }
}
