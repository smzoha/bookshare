package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.AuthorRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

/**
 * @author smzoha
 * @since 28/3/26
 **/
@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRequestService authorRequestService;

    @ResponseBody
    @PostMapping("/apply")
    public ResponseEntity<?> applyAsAuthor(@AuthenticationPrincipal LoginDetails loginDetails) {
        Login login = authorRequestService.getValidLoginForRequest(loginDetails.getEmail());

        if (Objects.isNull(login)) {
            return ResponseEntity.badRequest().build();
        }

        authorRequestService.saveAuthorRequest(login);

        return ResponseEntity.ok().build();
    }
}
