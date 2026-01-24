package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.login.LoginService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author smzoha
 * @since 12/12/25
 **/
@Controller
public class ShelfController {

    private final ShelfRepository shelfRepository;
    private final LoginService loginService;

    public ShelfController(ShelfRepository shelfRepository, LoginService loginService) {
        this.shelfRepository = shelfRepository;
        this.loginService = loginService;
    }

    @PostMapping("/shelf/add")
    public String createShelf(@RequestParam String name,
                              @RequestParam Long bookId,
                              @AuthenticationPrincipal LoginDetails loginDetails) {

        Login login = loginService.getLogin(loginDetails.getEmail());

        Shelf shelf = new Shelf(name, login);
        shelf = shelfRepository.save(shelf);

        return "redirect:/book/" + bookId;
    }
}
