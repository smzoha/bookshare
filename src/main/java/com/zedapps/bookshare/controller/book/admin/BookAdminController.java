package com.zedapps.bookshare.controller.book.admin;

import com.zedapps.bookshare.repository.book.BookRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author smzoha
 * @since 22/10/25
 **/
@Controller
@RequestMapping("/admin/books")
public class BookAdminController {

    private final BookRepository bookRepository;

    public BookAdminController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String getBookList(ModelMap model) {
        model.put("books", bookRepository.findAll());

        return "admin/books/bookList";
    }
}
