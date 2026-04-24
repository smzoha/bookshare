package com.zedapps.bookshare.controller.api;

import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.feed.FeedApiResponse;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.service.book.BookApiService;
import com.zedapps.bookshare.service.login.FeedApiService;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author smzoha
 * @since 24/4/26
 **/
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeApiController {

    private final BookRepository bookRepository;
    private final BookApiService bookApiService;
    private final FeedApiService feedApiService;
    private final LoginService loginService;

    @GetMapping("/featured")
    public ResponseEntity<List<BookDto>> getFeaturedBooks() {
        List<BookDto> featuredBookDtoList = bookRepository.getFeaturedBooks().stream()
                .map(book -> bookApiService.createDto(book, false))
                .toList();

        return ResponseEntity.ok().body(featuredBookDtoList);
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedApiResponse> getFeed(@RequestParam(defaultValue = "0", required = false) int page,
                                                   @AuthenticationPrincipal LoginDetails loginDetails) {

        Login login = loginService.getLogin(loginDetails.getEmail());

        FeedApiResponse feedApiResponse = feedApiService.getFeedApiResponse(login, 5, page);

        return ResponseEntity.ok().body(feedApiResponse);
    }
}
