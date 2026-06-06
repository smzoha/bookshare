package com.zedapps.bookshare.controller.image;

import com.zedapps.bookshare.controller.AbstractWebMvcTest;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.security.WithMockLoginDetails;
import com.zedapps.bookshare.service.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author smzoha
 * @since 6/6/26
 **/
@WebMvcTest(ImageController.class)
@WithMockLoginDetails
public class ImageControllerTest extends AbstractWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageService imageService;

    private Image image;

    @BeforeEach
    void setUp() {
        image = new Image();
        image.setId(1L);
        image.setFileName("test.png");
        image.setContentType("image/png");
        image.setUploadDate(LocalDateTime.now());
        image.setContent("dummy-image-data".getBytes());
    }

    @Test
    void getImage_validId_returnResponse() throws Exception {
        when(imageService.getImage(image.getId())).thenReturn(Optional.of(image));

        mockMvc.perform(get("/image/" + image.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, image.getContentType()))
                .andExpect(header().exists(HttpHeaders.CONTENT_LENGTH))
                .andExpect(content().bytes(image.getContent()));

        verify(imageService).getImage(image.getId());
    }

    @Test
    void getImage_imageNotFound_returnNotFound() throws Exception {
        when(imageService.getImage(image.getId())).thenReturn(Optional.empty());

        mockMvc.perform(get("/image/" + image.getId()))
                .andExpect(status().isNotFound());

        verify(imageService).getImage(image.getId());
    }

    @Test
    void uploadImage_validRequest_returnResponse() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.png",
                MediaType.IMAGE_PNG_VALUE, "dummy-image-data".getBytes());

        when(imageService.saveImage(any(Image.class))).thenReturn(image);

        mockMvc.perform(multipart("/image/upload").file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(imageService).saveImage(argThat(img -> "test.png".equals(img.getFileName())
                && MediaType.IMAGE_PNG_VALUE.equals(img.getContentType())
                && Arrays.equals("dummy-image-data".getBytes(), img.getContent())));
    }

    @Test
    void uploadImage_missingFilePart_returnBadRequest() throws Exception {
        mockMvc.perform(multipart("/image/upload"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(imageService);
    }
}
