package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import jakarta.persistence.NoResultException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author smzoha
 * @since 12/12/25
 **/
@Controller
public class ShelfController {

    private final ShelfRepository shelfRepository;
    private final LoginRepository loginRepository;

    public ShelfController(ShelfRepository shelfRepository, LoginRepository loginRepository) {
        this.shelfRepository = shelfRepository;
        this.loginRepository = loginRepository;
    }

    @PostMapping("/shelf/add")
    public String createShelf(@RequestParam String name,
                              @RequestParam Long bookId,
                              @AuthenticationPrincipal LoginDetails loginDetails,
                              ModelMap model) {

        Login login = loginRepository.findByEmail(loginDetails.getEmail()).orElseThrow(NoResultException::new);

        Shelf shelf = createShelf(name, login);
        shelf = shelfRepository.save(shelf);

        return "redirect:/book/" + bookId;
    }

    private Shelf createShelf(String name, Login login) {
        Shelf shelf = new Shelf();
        shelf.setName(name);
        shelf.setUser(login);

        return shelf;
    }
}
