package com.zedapps.bookshare.controller.book.app;

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
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.service.book.BookAdminService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author smzoha
 * @since 29/3/26
 **/
@Controller
@RequestMapping("/author/bookRequest")
@RequiredArgsConstructor
public class BookRequestController {

    private final LoginService loginService;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final BookRepository bookRepository;
    private final BookAdminService bookAdminService;

    @ModelAttribute("statusList")
    public Status[] getStatusList() {
        return new Status[]{Status.PENDING};
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

    @ModelAttribute("actionUrl")
    public String getActionUrl() {
        return "/author/bookRequest";
    }

    @InitBinder
    @SuppressWarnings("DuplicatedCode")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Author.class, new AuthorEditor(authorRepository));
        binder.registerCustomEditor(Genre.class, new GenreEditor(genreRepository));
        binder.registerCustomEditor(Tag.class, new TagEditor(tagRepository));
        binder.registerCustomEditor(Image.class, new ImageEditor(imageRepository));

        binder.setDisallowedFields("reviews*");
    }

    @GetMapping
    public String getBookRequestForm(@AuthenticationPrincipal LoginDetails loginDetails,
                                     ModelMap model) {

        Login login = loginService.getLogin(loginDetails.getEmail());
        Author author = authorRepository.findAuthorByLogin(login).orElse(null);

        assert login.getRole() == Role.AUTHOR && author != null : "User is not an author!";

        Book book = new Book();
        book.getAuthors().add(author);

        model.put("book", book);

        return "common/bookForm";
    }

    @PostMapping
    public String saveBookRequestForm(@Valid @ModelAttribute Book book, Errors errors) {
        if (errors.hasErrors()) {
            return "common/bookForm";
        }

        if (book.getImage() != null && book.getImage().getId() == null) {
            book.setImage(null);
        }

        bookAdminService.saveBook(book);

        return "redirect:/";
    }
}
