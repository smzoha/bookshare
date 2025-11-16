package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.editor.AuthorEditor;
import com.zedapps.bookshare.editor.GenreEditor;
import com.zedapps.bookshare.editor.ImageEditor;
import com.zedapps.bookshare.editor.TagEditor;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.book.enums.Status;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.repository.image.ImageRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author smzoha
 * @since 22/10/25
 **/
@Controller
@RequestMapping("/admin/book")
public class BookAdminController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;

    public BookAdminController(BookRepository bookRepository, AuthorRepository authorRepository,
                               GenreRepository genreRepository, TagRepository tagRepository,
                               ImageRepository imageRepository) {

        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.imageRepository = imageRepository;
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
    public String getBookList(ModelMap model) {
        model.put("books", bookRepository.findAll());

        return "admin/book/bookList";
    }

    @GetMapping("/new")
    public String createNewBook(ModelMap model) {
        model.put("book", new Book());

        return "admin/book/bookForm";
    }

    @GetMapping("/{id}")
    public String saveBook(@PathVariable long id, ModelMap model) {
        model.put("book", bookRepository.findBookById(id).orElseThrow(NoResultException::new));

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

        bookRepository.save(book);

        return "redirect:/admin";
    }
}
