package com.zedapps.bookshare.controller;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import com.zedapps.bookshare.service.login.FeedService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final LoginService loginService;
    private final ProfileService profileService;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final FeedService feedService;

    @GetMapping
    public String getHome(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        model.put("featuredBooks", bookRepository.getFeaturedBooks());
        model.put("genres", genreRepository.findAll().stream().sorted(Comparator.comparing(Genre::getName)).toList());

        if (loginDetails != null) {
            Login login = loginService.getLogin(loginDetails.getUsername());

            model.put("login", login);
            model.put("readingProgressList", profileService.getDistinctReadingProgressList(login));
        }

        return "home";
    }

    @GetMapping("/feed")
    public String getFeed(@AuthenticationPrincipal LoginDetails loginDetails,
                          @RequestParam(defaultValue = "0") int page,
                          ModelMap model) {

        setupFeed(loginService.getLogin(loginDetails.getEmail()), page, model);

        return "app/userFeedFragment :: userFeed";
    }


    @GetMapping("/admin")
    public String getAdminHome() {
        return "adminHome";
    }

    private void setupFeed(Login audience, int page, ModelMap model) {
        Page<FeedEntry> feedEntries = feedService.getFeedEntries(audience, 5, page);
        List<FeedDto> feedDtoList = feedService.mapToFeedDtoList(feedEntries);

        model.put("feedDtoList", feedDtoList);
        model.put("currentPage", page);
        model.put("totalPages", feedEntries.getTotalPages() - 1);
    }
}
