package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.service.book.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Controller
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;

    public BookController(BookService bookService, GenreRepository genreRepository, TagRepository tagRepository) {
        this.bookService = bookService;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/list")
    public String getBookList(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) String rating,
                              @RequestParam(required = false) String genre,
                              @RequestParam(required = false) String tag,
                              ModelMap model,
                              HttpServletRequest request) {

        model.put("bookPage", bookService.getPaginatedBooks(page, sort, rating, genre, tag));

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "app/book/bookGridFragment :: bookGrid";
        }

        model.put("genres", genreRepository.findAll().stream().sorted(Comparator.comparing(Genre::getName)));
        model.put("tags", tagRepository.findAll().stream().sorted(Comparator.comparing(Tag::getName)));

        return "app/book/bookList";
    }

    @GetMapping("/{id}")
    public String getBookPage(@AuthenticationPrincipal LoginDetails loginDetails,
                              @PathVariable Long id,
                              ModelMap model) {

        bookService.setupReferenceData(loginDetails, id, model);
        model.put("reviewDto", new BookReviewDto());

        return "app/book/book";
    }

    @GetMapping("/reviews/{id}")
    public String getReviewPage(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page,
                                ModelMap model) {

        Book book = bookService.getBook(id);
        Page<Review> reviews = bookService.getReviewsByBook(book, page);

        model.put("reviews", reviews);

        return "app/book/reviewList :: reviewList";
    }

    @PostMapping("/addReview")
    public String addReview(@Valid @ModelAttribute("reviewDto") BookReviewDto reviewDto,
                            Errors errors,
                            @AuthenticationPrincipal LoginDetails loginDetails,
                            ModelMap model) {

        Assert.notNull(loginDetails, "User is not logged in!");

        if (errors.hasErrors()) {
            bookService.setupReferenceData(loginDetails, reviewDto.getBookId(), model);
            return "app/book/book";
        }

        Review review = bookService.saveReview(reviewDto, loginDetails);

        return "redirect:/book/" + review.getBook().getId();
    }

    @ResponseBody
    @PostMapping("/like")
    public ResponseEntity<ReviewLikeResponseDto> toggleLike(@RequestParam Long reviewId,
                                                            @AuthenticationPrincipal LoginDetails loginDetails) {

        Assert.notNull(loginDetails, "User is not logged in!");

        ReviewLikeResponseDto responseDto = bookService.updateReviewLikes(reviewId, loginDetails);

        return ResponseEntity.ok().body(responseDto);
    }
}
