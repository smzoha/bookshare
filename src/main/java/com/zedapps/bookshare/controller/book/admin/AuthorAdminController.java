package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.book.AuthorService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Controller
@RequestMapping("/admin/author")
public class AuthorAdminController {

    private final AuthorService authorService;
    private final LoginService loginService;
    private final ApplicationEventPublisher publisher;

    public AuthorAdminController(AuthorService authorService, LoginService loginService,
                                 ApplicationEventPublisher publisher) {

        this.authorService = authorService;
        this.loginService = loginService;
        this.publisher = publisher;
    }

    @ModelAttribute("loginList")
    public List<Login> loginList() {
        return loginService.getActiveLoginList();
    }

    @GetMapping("/list")
    public String getAuthorList(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        model.put("authors", authorService.getAuthorList());

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
                .eventType(ActivityType.AUTHOR_LIST_VIEW)
                .metadata(Collections.singletonMap("actionBy", loginDetails.getEmail()))
                .internal(true)
                .build());

        return "admin/book/authorList";
    }

    @GetMapping("/new")
    public String createNewAuthor(ModelMap model) {
        model.put("author", new Author());

        return "admin/book/authorForm";
    }

    @GetMapping("{id}")
    public String getAuthor(@AuthenticationPrincipal LoginDetails loginDetails, @PathVariable Long id, ModelMap model) {
        Author author = authorService.getAuthor(id);
        model.put("author", author);

        publisher.publishEvent(ActivityEvent.builder()
                .login(loginService.getLogin(loginDetails.getEmail()))
                .eventType(ActivityType.AUTHOR_VIEW)
                .metadata(Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "affectedAuthorId", author.getId(),
                        "affectedAuthorLogin", author.getLogin() != null ? author.getLogin().getId() : ""
                ))
                .internal(true)
                .build());

        return "admin/book/authorForm";
    }

    @PostMapping("/save")
    public String saveAuthor(@Valid @ModelAttribute Author author,
                             Errors errors) {

        validateAuthorLoginLink(author, errors);

        if (errors.hasErrors()) {
            return "admin/book/authorForm";
        }

        authorService.saveAuthor(author);

        return "redirect:/admin";
    }

    private void validateAuthorLoginLink(Author author, Errors errors) {
        Optional<Author> authorOptional = authorService.getAuthorByLogin(author.getLogin());

        if (authorOptional.isPresent() && !Objects.equals(authorOptional.get().getId(), author.getId())) {
            errors.rejectValue("login", "error.input.exists");
        }
    }
}
