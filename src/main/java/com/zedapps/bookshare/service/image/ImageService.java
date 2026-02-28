package com.zedapps.bookshare.service.image;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * @author smzoha
 * @since 15/2/26
 **/
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ActivityService activityService;

    public Optional<Image> getImage(Long id) {
        return imageRepository.findById(id);
    }

    public Image saveImage(Image image) {
        image = imageRepository.save(image);

        LoginDetails loginDetails = (LoginDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        activityService.saveActivityOutbox(ActivityType.IMAGE_UPLOAD,
                image.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedImageId", image.getId(),
                        "fileName", image.getFileName(),
                        "fileContentType", image.getContentType()
                ));

        return image;
    }
}
