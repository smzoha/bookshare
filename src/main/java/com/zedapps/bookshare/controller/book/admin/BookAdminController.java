package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.editor.AuthorEditor;
import com.zedapps.bookshare.editor.GenreEditor;
import com.zedapps.bookshare.editor.ImageEditor;
import com.zedapps.bookshare.editor.TagEditor;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.book.enums.Status;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.validation.Valid;
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
public class BookAdminController {

    private final BookService bookService;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final LoginService loginService;
    private final ApplicationEventPublisher publisher;

    public BookAdminController(BookService bookService, AuthorRepository authorRepository,
                               GenreRepository genreRepository, TagRepository tagRepository,
                               ImageRepository imageRepository, LoginService loginService,
                               ApplicationEventPublisher publisher) {

        this.bookService = bookService;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.imageRepository = imageRepository;
        this.loginService = loginService;
        this.publisher = publisher;
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
        model.put("books", bookService.getBookList());

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
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
        model.put("book", bookService.getBook(id));

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
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

        book = bookService.saveBook(book);

        return "redirect:/admin";
    }
}
