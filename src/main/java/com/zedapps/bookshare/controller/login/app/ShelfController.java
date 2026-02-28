package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.login.ShelfService;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author smzoha
 * @since 12/12/25
 **/
@Controller
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;
    private final LoginService loginService;

    @PostMapping("/shelf/add")
    public String createShelf(@RequestParam String name,
                              @RequestParam Long bookId,
                              @AuthenticationPrincipal LoginDetails loginDetails) {

        Login login = loginService.getLogin(loginDetails.getEmail());

        Shelf shelf = new Shelf(name, login);
        shelfService.saveShelf(shelf, loginDetails);

        return "redirect:/book/" + bookId;
    }
}
