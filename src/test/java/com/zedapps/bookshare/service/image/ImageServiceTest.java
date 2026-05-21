package com.zedapps.bookshare.service.image;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 10/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ActivityService activityService;

    private Image image;

    @Captor
    private ArgumentCaptor<Map<String, Object>> activityOutboxPayloadCaptor;

    @BeforeEach
    void setup() {
        image = new Image();
        image.setId(1L);
        image.setFileName("test.txt");
        image.setContent("This is a test text".getBytes(StandardCharsets.UTF_8));
        image.setContentType("text/plain");

        LoginDetails loginDetails = TestUtils.getLoginDetails("test@test.com", "test", true);
        TestUtils.setupSecurityContext(loginDetails);

        lenient().when(imageRepository.save(any(Image.class))).thenReturn(image);
    }

    @AfterEach
    void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getImage_existingId_returnsNonEmptyOptional() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        Optional<Image> imageOptional = imageService.getImage(image.getId());

        assertThat(imageOptional).isPresent().contains(image);
    }

    @Test
    void getImage_nonExistingId_returnsEmptyOptional() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<Image> imageOptional = imageService.getImage(image.getId());

        assertThat(imageOptional).isEmpty();
    }

    @Test
    void saveImage_validImage_persistsAndReturnsImage() {
        Image persistedImage = imageService.saveImage(image);

        assertThat(persistedImage).isEqualTo(image);
        verify(imageRepository).save(image);
    }

    @Test
    void saveImage_validImage_firesImageUploadActivityOutbox() {
        imageService.saveImage(image);

        verify(activityService).saveActivityOutbox(eq(ActivityType.IMAGE_UPLOAD),
                eq(image.getId()), anyMap());
    }

    @Test
    void saveImage_validImage_includesCorrectMetadataInOutbox() {
        imageService.saveImage(image);

        verify(activityService).saveActivityOutbox(eq(ActivityType.IMAGE_UPLOAD),
                eq(image.getId()),
                activityOutboxPayloadCaptor.capture());

        assertThat(activityOutboxPayloadCaptor.getValue())
                .containsEntry("actionBy", "test@test.com")
                .containsEntry("affectedImageId", image.getId())
                .containsEntry("fileName", image.getFileName())
                .containsEntry("fileContentType", image.getContentType());
    }
}
