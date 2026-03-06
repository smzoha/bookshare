package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.service.book.BookAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/genre")
@RequiredArgsConstructor
public class GenreAdminController {

    private final BookAdminService bookAdminService;
    private final ApplicationEventPublisher publisher;

    @GetMapping
    public String getGenreList(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        model.put("genres", bookAdminService.getGenreList());

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.GENRE_LIST_VIEW)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        return "admin/genre/genreList";
    }

    @GetMapping("/new")
    public String createGenre(ModelMap model) {
        model.put("genre", new Genre());

        return "admin/genre/genreForm";
    }

    @GetMapping("{id}")
    public String getGenre(@AuthenticationPrincipal LoginDetails loginDetails, @PathVariable Long id, ModelMap model) {
        Genre genre = bookAdminService.getGenre(id);
        model.put("genre", genre);

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.GENRE_VIEW)
                .metadata(Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedGenreId", genre.getId()
                ))
                .internal(true)
                .build());

        return "admin/genre/genreForm";
    }

    @PostMapping("/save")
    public String saveGenre(@Valid @ModelAttribute Genre genre,
                            Errors errors) {

        validateGenreUniqueness(genre, errors);

        if (errors.hasErrors()) {
            return "admin/genre/genreForm";
        }

        bookAdminService.saveGenre(genre);

        return "redirect:/admin";
    }

    private void validateGenreUniqueness(Genre genre, Errors errors) {
        if (StringUtils.isBlank(genre.getName())) {
            return;
        }

        Optional<Genre> genreOptional = bookAdminService.getGenreByName(genre.getName());

        if (genreOptional.isPresent() && !Objects.equals(genreOptional.get().getId(), genre.getId())) {
            errors.rejectValue("name", "error.input.exists");
        }
    }
}
