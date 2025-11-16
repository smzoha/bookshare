package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.repository.book.GenreRepository;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/genre")
public class GenreAdminController {

    private final GenreRepository genreRepository;

    public GenreAdminController(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping
    public String getGenreList(ModelMap model) {
        model.put("genres", genreRepository.findAll());

        return "admin/genre/genreList";
    }

    @GetMapping("/new")
    public String createGenre(ModelMap model) {
        model.put("genre", new Genre());

        return "admin/genre/genreForm";
    }

    @GetMapping("{id}")
    public String getGenre(@PathVariable Long id, ModelMap model) {
        Genre genre = genreRepository.findById(id).orElseThrow(NoResultException::new);
        model.put("genre", genre);

        return "admin/genre/genreForm";
    }

    @PostMapping("/save")
    public String saveGenre(@Valid @ModelAttribute Genre genre,
                            Errors errors) {

        validateGenreUniqueness(genre, errors);

        if (errors.hasErrors()) {
            return "admin/genre/genreForm";
        }

        genreRepository.save(genre);

        return "redirect:/admin";
    }

    private void validateGenreUniqueness(Genre genre, Errors errors) {
        if (StringUtils.isBlank(genre.getName())) {
            return;
        }

        Optional<Genre> genreOptional = genreRepository.findGenreByName(genre.getName());

        if (genreOptional.isPresent() && !Objects.equals(genreOptional.get().getId(), genre.getId())) {
            errors.rejectValue("name", "error.input.exists");
        }
    }
}
