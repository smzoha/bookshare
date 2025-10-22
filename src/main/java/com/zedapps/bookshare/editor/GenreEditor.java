package com.zedapps.bookshare.editor;

import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.repository.book.GenreRepository;

import java.beans.PropertyEditorSupport;
import java.util.Objects;

/**
 * @author smzoha
 * @since 22/10/25
 **/
public class GenreEditor extends PropertyEditorSupport {

    private final GenreRepository genreRepository;

    public GenreEditor(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    public String getAsText() {
        Genre genre = (Genre) getValue();

        return Objects.nonNull(genre) ? String.valueOf(genre.getId()) : null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Genre genre = genreRepository.findById(Long.parseLong(text)).orElse(null);
        setValue(genre);
    }
}
