package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import jakarta.persistence.NoResultException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/author")
public class AuthorAdminController {

    private final AuthorRepository authorRepository;
    private final LoginRepository loginRepository;

    public AuthorAdminController(AuthorRepository authorRepository, LoginRepository loginRepository) {
        this.authorRepository = authorRepository;
        this.loginRepository = loginRepository;
    }

    @ModelAttribute("loginList")
    public List<Login> loginList() {
        return loginRepository.findAllByActive(true);
    }

    @GetMapping("/list")
    public String getAuthorList(ModelMap model) {
        model.put("authors", authorRepository.findAll());

        return "admin/books/authorList";
    }

    @GetMapping("/new")
    public String createNewAuthor(ModelMap model) {
        model.put("author", new Author());

        return "admin/books/authorForm";
    }

    @GetMapping("{id}")
    public String getAuthor(@PathVariable Long id, ModelMap model) {
        model.put("author", authorRepository.findById(id).orElseThrow(NoResultException::new));

        return "admin/books/authorForm";
    }

    @PostMapping("/save")
    public String saveAuthor(@Valid @ModelAttribute Author author,
                             Errors errors) {

        validateAuthorLoginLink(author, errors);

        if (errors.hasErrors()) {
            return "admin/books/authorForm";
        }

        authorRepository.save(author);

        return "redirect:/admin";
    }

    private void validateAuthorLoginLink(Author author, Errors errors) {
        Optional<Author> authorOptional = authorRepository.findAuthorByLogin(author.getLogin());

        if (authorOptional.isPresent() && !Objects.equals(authorOptional.get().getId(), author.getId())) {
            errors.rejectValue("login", "error.input.exists");
        }
    }
}
