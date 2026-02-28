package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author smzoha
 * @since 21/2/26
 **/
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final LoginService loginService;

    @GetMapping
    public String getProfile(@AuthenticationPrincipal LoginDetails loginDetails,
                             ModelMap model) {

        profileService.setupReferenceData(loginDetails, model);

        return "app/profile/profile";
    }

    @GetMapping("/shelf")
    public String getProfile(@AuthenticationPrincipal LoginDetails loginDetails,
                             @RequestParam Long shelfId,
                             ModelMap model) {

        Login login = loginService.getLogin(loginDetails.getEmail());
        model.put("activeShelf", login.getShelf(shelfId));

        return "app/profile/profileActiveShelfFragment :: activeShelfFragment";
    }
}
