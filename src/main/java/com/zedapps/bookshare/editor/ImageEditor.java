package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.image.ImageRepository;

import java.beans.PropertyEditorSupport;

/**
 * @author smzoha
 * @since 24/10/25
 **/
public class ImageEditor extends PropertyEditorSupport {

    private final ImageRepository imageRepository;

    public ImageEditor(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
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

    @Override
    public String getAsText() {
        Image image = (Image) getValue();

        return image != null ? String.valueOf(image.getId()) : null;
    }
}
