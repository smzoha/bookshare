package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.login.LoginService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author smzoha
 * @since 24/1/26
 **/
@Controller
@RequestMapping("/collection")
public class CollectionController {

    private final LoginService loginService;
    private final ShelfRepository shelfRepository;

    public CollectionController(LoginService loginService, ShelfRepository shelfRepository) {
        this.loginService = loginService;
        this.shelfRepository = shelfRepository;
    }

    @GetMapping
    public String getCollection(@AuthenticationPrincipal LoginDetails loginDetails, ModelMap model) {
        List<Shelf> userShelves = shelfRepository.findAllByUser_EmailOrderByName(loginDetails.getEmail());

        model.put("login", loginService.getLogin(loginDetails.getEmail()));
        model.put("collections", userShelves);
        model.put("active", userShelves.getFirst());

        return "app/login/collection";
    }
}
