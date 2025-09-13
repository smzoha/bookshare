package com.zedapps.bookshare.controller;

import com.zedapps.bookshare.repository.book.BookRepository;
import com.zedapps.bookshare.repository.book.GenreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Controller
@RequestMapping("/")
public class HomeController {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    public HomeController(BookRepository bookRepository, GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
    }

    @GetMapping
    public String getHome(ModelMap model) {
        model.put("featuredBooks", bookRepository.getFeaturedBooks());
        model.put("genres", genreRepository.findAll());

        return "home";
    }
}
