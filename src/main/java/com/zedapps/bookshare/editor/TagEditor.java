package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.repository.book.TagRepository;

import java.beans.PropertyEditorSupport;
import java.util.Objects;

/**
 * @author smzoha
 * @since 22/10/25
 **/
public class TagEditor extends PropertyEditorSupport {

    private final TagRepository tagRepository;

    public TagEditor(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public String getAsText() {
        Tag tag = (Tag) getValue();

        return Objects.nonNull(tag) ? String.valueOf(tag.getId()) : null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Tag tag = tagRepository.findById(Long.parseLong(text)).orElse(null);
        setValue(tag);
    }
}
