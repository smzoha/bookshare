package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.image.ImageRepository;
import lombok.RequiredArgsConstructor;

import java.beans.PropertyEditorSupport;

/**
 * @author smzoha
 * @since 24/10/25
 **/
@RequiredArgsConstructor
public class ImageEditor extends PropertyEditorSupport {

    private final ImageRepository imageRepository;

    @Override
    public String getAsText() {
        Image image = (Image) getValue();

        return image != null ? String.valueOf(image.getId()) : null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || text.trim().isEmpty()) {
            setValue(null);
        } else {
            Image image = imageRepository.findById(Long.parseLong(text)).orElse(null);
            setValue(image);
        }
    }
}
