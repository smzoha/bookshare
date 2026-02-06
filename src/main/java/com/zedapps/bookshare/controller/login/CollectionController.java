package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.login.LoginService;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;

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
    public String getCollection(@AuthenticationPrincipal LoginDetails loginDetails,
                                @RequestParam(required = false) Long shelfId,
                                ModelMap model,
                                HttpServletRequest request) {

        List<Shelf> userShelves = shelfRepository.getShelvesForCollection(loginDetails.getEmail());
        Login login = loginService.getLogin(loginDetails.getEmail());
        Shelf currentShelf;

        model.put("login", login);
        model.put("collections", userShelves);

        if (Objects.nonNull(shelfId)) {
            currentShelf = shelfRepository.findById(shelfId).orElseThrow(NoResultException::new);
            assert Objects.equals(currentShelf.getUser(), login);

        } else {
            currentShelf = userShelves.getFirst();
        }

        model.put("currentShelf", currentShelf);

        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                ? "app/login/fragments/shelvedBookFragment :: shelvedBooks"
                : "app/login/collection";
    }
}
