package com.zedapps.bookshare.controller.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.login.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author smzoha
 * @since 21/2/26
 **/
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final LoginService loginService;

    @GetMapping
    public String getProfile(@AuthenticationPrincipal LoginDetails loginDetails,
                             ModelMap model) {

        setupReferenceData(loginDetails, model);

        return "app/login/profile";
    }

    @GetMapping("/shelf")
    public String getProfile(@AuthenticationPrincipal LoginDetails loginDetails,
                             @RequestParam Long shelfId,
                             ModelMap model) {

        Login login = loginService.getLogin(loginDetails.getEmail());
        model.put("activeShelf", login.getShelf(shelfId));

        return "app/login/fragments/profileActiveShelfFragment :: activeShelfFragment";
    }

    private void setupReferenceData(LoginDetails loginDetails, ModelMap model) {
        Login login = loginService.getLogin(loginDetails.getEmail());

        model.put("login", login);
        model.put("totalBooks", login.getShelves()
                .stream()
                .mapToInt(shelf -> shelf.getBooks().size())
                .sum());

        setupShelves(model, login);

        model.put("readingProgress", login.getReadingProgresses()
                .stream()
                .filter(rp -> !rp.isCompleted())
                .collect(Collectors.toMap(
                        ReadingProgress::getBook,
                        rp -> rp,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .limit(5)
                .toList());
    }

    private void setupShelves(ModelMap model, Login login) {
        Map<Long, String> defaultShelves = new LinkedHashMap<>();
        Map<Long, String> shelves = new LinkedHashMap<>();
        Shelf activeShelf = null;

        for (Shelf shelf : login.getShelves()) {
            if (shelf.isDefaultShelf()) {
                defaultShelves.put(shelf.getId(), shelf.getName());
                if (activeShelf == null) activeShelf = shelf;

            } else {
                shelves.put(shelf.getId(), shelf.getName());
            }
        }

        model.put("defaultShelves", defaultShelves);
        model.put("shelves", shelves);
        model.put("activeShelf", activeShelf);
    }
}
