package com.zedapps.bookshare.controller.book;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

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

        bookService.setupReferenceData(loginDetails, id, model);
        model.put("reviewDto", new BookReviewDto());

        return "app/book/book";
    }

    @PostMapping("/addReview")
    public String addReview(@Valid @ModelAttribute("reviewDto") BookReviewDto reviewDto,
                            Errors errors,
                            @AuthenticationPrincipal LoginDetails loginDetails,
                            ModelMap model) {

        assert Objects.nonNull(loginDetails);

        if (errors.hasErrors()) {
            bookService.setupReferenceData(loginDetails, reviewDto.getBookId(), model);
            return "app/book/book";
        }

        Review review = bookService.saveReview(reviewDto, loginDetails);

        return "redirect:/book/" + review.getBook().getId();
    }
}
