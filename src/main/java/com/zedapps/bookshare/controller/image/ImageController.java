package com.zedapps.bookshare.controller.image;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.image.ImageRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * @author smzoha
 * @since 13/9/25
 **/
@Controller
@RequestMapping("/image")
public class ImageController {

    private final ImageRepository imageRepository;

    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<Image> imageOptional = imageRepository.findById(id);

        if (imageOptional.isEmpty()) return ResponseEntity.notFound().build();

        Image image = imageOptional.get();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, image.getContentType());
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(image.getContent().length));

        return new ResponseEntity<>(image.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<Long> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        Image image = Image.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .content(file.getBytes())
                .build();

        image = imageRepository.save(image);

        return ResponseEntity.ok().body(image.getId());
    }
}