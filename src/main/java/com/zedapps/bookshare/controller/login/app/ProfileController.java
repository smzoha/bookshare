package com.zedapps.bookshare.controller.login.app;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 21/2/26
 **/
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final List<ConnectionAction> FRIEND_ACCEPT_OR_REMOVE_ACTIONS = List.of(ConnectionAction.ACCEPT_FRIEND_REQ,
            ConnectionAction.REMOVE_FRIEND);

    private final ProfileService profileService;
    private final LoginService loginService;

    @GetMapping
    public String getProfile(@AuthenticationPrincipal LoginDetails loginDetails) {
        return "redirect:/profile/" + loginDetails.getHandle();
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

    @PostMapping("/friendRequest")
    public String sendOrRevokeFriendRequest(@AuthenticationPrincipal LoginDetails loginDetails,
                                            @RequestParam String handle,
                                            @RequestParam ConnectionAction action,
                                            ModelMap model) {

        Login authLogin = loginService.getLogin(loginDetails.getEmail());
        Login profileLogin = loginService.getLoginByHandle(handle);

        if (Objects.equals(authLogin.getEmail(), profileLogin.getEmail())) {
            throw new IllegalArgumentException("You cannot send friend requests to yourself!");
        }

        profileService.performConnectionAction(authLogin, profileLogin, action);

        if (FRIEND_ACCEPT_OR_REMOVE_ACTIONS.contains(action)) {
            profileService.setupReferenceData(profileLogin.getEmail(), loginDetails, model);

            return "app/profile/profile";

        } else {
            model.put("login", profileLogin);
            profileService.setupConnectionRefData(model, profileLogin, authLogin);

            return "app/profile/profileInfoFragment :: profileInfoFragment";
        }
    }
}
