package com.zedapps.bookshare.controller.book;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Controller
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;
    private final LoginService loginService;

    public BookController(BookService bookService, LoginService loginService) {
        this.bookService = bookService;
        this.loginService = loginService;
    }

    @GetMapping("/{id}")
    public String book(@AuthenticationPrincipal LoginDetails loginDetails,
                       @PathVariable Long id,
                       ModelMap model) {

        Book book = bookService.getBook(id);
        model.put("book", book);

        if (Objects.nonNull(loginDetails)) {
            Login login = loginService.getLogin(loginDetails.getEmail());
            model.put("shelves", login.getShelves());
        }

        model.put("reviews", bookService.getReviewsByBook(book, 0));
        model.put("relatedBooks", bookService.getRelatedBooks(book));

        return "app/book/book";
    }
}
