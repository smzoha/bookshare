package com.zedapps.bookshare.controller.api.login;

import com.zedapps.bookshare.dto.api.ErrorResponseDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.service.login.AuthorRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 24/4/26
 **/
@RestController
@RequestMapping("/api/v1/author")
@RequiredArgsConstructor
public class AuthorApiController {

    private final AuthorRequestService authorRequestService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyAsAuthor(@AuthenticationPrincipal LoginDetails loginDetails) {
        Login login = authorRequestService.getValidLoginForRequest(loginDetails.getEmail());

        if (Objects.isNull(login)) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto(List.of("error.invalid")));
        }

        authorRequestService.saveAuthorRequest(login);

        return ResponseEntity.ok().build();
    }
}
