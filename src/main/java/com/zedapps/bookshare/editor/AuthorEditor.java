package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.repository.login.AuthorRepository;

import java.beans.PropertyEditorSupport;
import java.util.Objects;

/**
 * @author smzoha
 * @since 22/10/25
 **/
public class AuthorEditor extends PropertyEditorSupport {

    private final AuthorRepository authorRepository;

    public AuthorEditor(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public String getAsText() {
        Author author = (Author) getValue();

        return Objects.nonNull(author) ? String.valueOf(author.getId()) : null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Author author = authorRepository.findById(Long.parseLong(text)).orElse(null);
        setValue(author);
    }
}
