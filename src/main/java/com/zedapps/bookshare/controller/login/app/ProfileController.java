package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

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

        profileService.setupReferenceData(loginDetails.getEmail(), loginDetails, model);

        return "app/profile/profile";
    }

    @GetMapping("/{handle}")
    public String getProfileByHandle(@AuthenticationPrincipal LoginDetails loginDetails,
                                     @PathVariable String handle,
                                     ModelMap model) {

        Login login = loginService.getLoginByHandle(handle);

        profileService.setupReferenceData(login.getEmail(), loginDetails, model);

        return "app/profile/profile";
    }

    @GetMapping("/shelf")
    public String getShelf(@AuthenticationPrincipal LoginDetails loginDetails,
                           @RequestParam Long shelfId,
                           ModelMap model) {

        Login login = loginService.getLogin(loginDetails.getEmail());

        model.put("activeShelf", login.getShelf(shelfId));

        return "app/profile/profileActiveShelfFragment :: activeShelfFragment";
    }

    @GetMapping("/{handle}/shelf")
    public String getShelfForHandle(@PathVariable String handle, @RequestParam Long shelfId, ModelMap model) {
        Login login = loginService.getLoginByHandle(handle);

        model.put("activeShelf", login.getShelf(shelfId));

        return "app/profile/profileActiveShelfFragment :: activeShelfFragment";
    }

    @PostMapping("/sendRevokeFriendReq")
    public String sendOrRevokeFriendRequest(@AuthenticationPrincipal LoginDetails loginDetails,
                                            @RequestParam String handle,
                                            @RequestParam boolean send,
                                            ModelMap model) {

        Login authLogin = loginService.getLogin(loginDetails.getEmail());
        Login profileLogin = loginService.getLoginByHandle(handle);

        if (send) {
            profileService.saveFriendRequest(authLogin, profileLogin);
        } else {
            profileService.revokeFriendRequest(authLogin, profileLogin);
        }

        model.put("login", profileLogin);
        profileService.setupFriendFlags(model, profileLogin, authLogin);

        return "app/profile/profileInfoFragment :: profileInfoFragment";
    }
}
