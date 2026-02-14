package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.dto.book.ReviewLikeResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.repository.book.TagRepository;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

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
    private final LoginService loginService;
    private final ApplicationEventPublisher publisher;

    public BookController(BookService bookService, GenreRepository genreRepository,
                          TagRepository tagRepository, LoginService loginService,
                          ApplicationEventPublisher publisher) {

        this.bookService = bookService;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.loginService = loginService;
        this.publisher = publisher;
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
                              @AuthenticationPrincipal LoginDetails loginDetails,
                              ModelMap model,
                              HttpServletRequest request) {

        model.put("bookPage", bookService.getPaginatedBooks(page, sort, rating, genre, tag));

        if (loginDetails != null) {
            publisher.publishEvent(ActivityEvent.builder()
                    .login(loginService.getLogin(loginDetails.getEmail()))
                    .eventType(ActivityType.BOOK_LIST_VIEW)
                    .metadata(Map.of(
                            "actionBy", loginDetails.getEmail(),
                            "page", Objects.toString(page, ""),
                            "sort", Objects.toString(sort, ""),
                            "rating", Objects.toString(rating, ""),
                            "genre", Objects.toString(genre, ""),
                            "tag", Objects.toString(tag, "")
                    ))
                    .internal(true)
                    .build());
        }

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "app/book/bookGridFragment :: bookGrid";
        }

        model.put("genres", genreRepository.findAll().stream().sorted(Comparator.comparing(Genre::getName)).toList());
        model.put("tags", tagRepository.findAll().stream().sorted(Comparator.comparing(Tag::getName)).toList());

        return "app/book/bookList";
    }

    @GetMapping("/{id}")
    public String getBookPage(@AuthenticationPrincipal LoginDetails loginDetails,
                              @PathVariable Long id,
                              ModelMap model) {

        bookService.setupReferenceData(loginDetails, id, model, true, true);

        if (loginDetails != null) {
            publisher.publishEvent(ActivityEvent.builder()
                    .login(loginService.getLogin(loginDetails.getEmail()))
                    .eventType(ActivityType.BOOK_VIEW)
                    .referenceId(id)
                    .metadata(Map.of(
                            "actionBy", loginDetails.getEmail(),
                            "bookId", id
                    ))
                    .internal(true)
                    .build());
        }

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

        if (errors.hasErrors()) {
            bookService.setupReferenceData(loginDetails, reviewDto.getBookId(), model, false, true);
            return "app/book/book";
        }

        Review review = bookService.saveReview(reviewDto, loginDetails);

        return "redirect:/book/" + review.getBook().getId();
    }

    @ResponseBody
    @PostMapping("/addShelf")
    public ResponseEntity<?> addToShelf(@AuthenticationPrincipal LoginDetails loginDetails,
                                        @RequestParam Long bookId,
                                        @RequestParam Long shelfId) {

        bookService.addToShelf(loginDetails, bookId, shelfId);

        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/removeShelf")
    public ResponseEntity<?> removeFromShelf(@AuthenticationPrincipal LoginDetails loginDetails,
                                             @RequestParam Long bookId,
                                             @RequestParam Long shelfId) {

        bookService.removeFromShelf(loginDetails, bookId, shelfId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/updateProgress")
    public String updateReadingProgress(@Valid @ModelAttribute("tmpProgress") ReadingProgress readingProgress,
                                        Errors errors,
                                        @AuthenticationPrincipal LoginDetails loginDetails,
                                        ModelMap model) {

        if (errors.hasErrors()) {
            bookService.setupReferenceData(loginDetails, readingProgress.getBook().getId(), model, true, false);
            model.put("showReadingProgressModal", true);

            return "app/book/book";
        }

        readingProgress = bookService.saveReadingProgress(readingProgress, loginDetails);

        return "redirect:/book/" + readingProgress.getBook().getId();
    }

    @ResponseBody
    @PostMapping("/like")
    public ResponseEntity<ReviewLikeResponseDto> toggleLike(@RequestParam Long reviewId,
                                                            @AuthenticationPrincipal LoginDetails loginDetails) {

        ReviewLikeResponseDto responseDto = bookService.updateReviewLikes(reviewId, loginDetails);

        return ResponseEntity.ok().body(responseDto);
    }
}
