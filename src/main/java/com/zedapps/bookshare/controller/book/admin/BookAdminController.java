package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.editor.AuthorEditor;
import com.zedapps.bookshare.editor.GenreEditor;
import com.zedapps.bookshare.editor.ImageEditor;
import com.zedapps.bookshare.editor.TagEditor;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.service.book.BookAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author smzoha
 * @since 22/10/25
 **/
@Controller
@RequestMapping("/admin/book")
@RequiredArgsConstructor
public class BookAdminController {

    private final BookAdminService bookAdminService;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final ApplicationEventPublisher publisher;

    @ModelAttribute("actionUrl")
    public String getActionUrl() {
        return "/admin/book/save";
    }

    @ModelAttribute("statusList")
    public Status[] getStatusList() {
        return Status.values();
    }

    @ModelAttribute("authorList")
    public List<Author> getAuthorList() {
        return authorRepository.findAll();
    }

    @ModelAttribute("genreList")
    public List<Genre> getGenreList() {
        return genreRepository.findAll();
    }

    @ModelAttribute("tagList")
    public List<Tag> getTagList() {
        return tagRepository.findAll();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Author.class, new AuthorEditor(authorRepository));
        binder.registerCustomEditor(Genre.class, new GenreEditor(genreRepository));
        binder.registerCustomEditor(Tag.class, new TagEditor(tagRepository));
        binder.registerCustomEditor(Image.class, new ImageEditor(imageRepository));

        binder.setDisallowedFields("reviews*");
    }

    @GetMapping
    public String getBookList(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        model.put("books", bookAdminService.getBookList());

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.BOOK_LIST_VIEW_ADMIN)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        return "admin/book/bookList";
    }

    @GetMapping("/new")
    public String createNewBook(ModelMap model) {
        model.put("book", new Book());

        return "admin/book/bookForm";
    }

    @GetMapping("/{id}")
    public String updateBook(@AuthenticationPrincipal LoginDetails loginDetails, @PathVariable long id, ModelMap model) {
        model.put("book", bookAdminService.getBook(id));

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.BOOK_VIEW_ADMIN)
                .metadata(Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedBookId", id
                ))
                .internal(true)
                .build());

        return "admin/book/bookForm";
    }

    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute Book book,
                           Errors errors) {

        if (errors.hasErrors()) {
            return "admin/book/bookForm";
        }

        if (book.getImage() != null && book.getImage().getId() == null) {
            book.setImage(null);
        }

        bookAdminService.saveBook(book);

        return "redirect:/admin";
    }
}
