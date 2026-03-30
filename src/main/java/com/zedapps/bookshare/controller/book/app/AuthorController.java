package com.zedapps.bookshare.controller.book.app;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.AuthorRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.book.AuthorRequestRepository;
import com.zedapps.bookshare.repository.login.AuthorRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Optional;

/**
 * @author smzoha
 * @since 28/3/26
 **/
@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorController {

    private final LoginRepository loginRepository;
    private final AuthorRepository authorRepository;
    private final AuthorRequestRepository authorRequestRepository;
    private final ApplicationEventPublisher publisher;

    @ResponseBody
    @PostMapping("/apply")
    public ResponseEntity<?> applyAsAuthor(@AuthenticationPrincipal LoginDetails loginDetails) {
        boolean isValid = isValidRequest(loginDetails.getEmail());

        if (!isValid) {
            return ResponseEntity.badRequest().build();
        }

        Login login = loginRepository.findByEmail(loginDetails.getEmail()).get();

        authorRequestRepository.save(new AuthorRequest(login));

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(loginDetails.getEmail())
                .eventType(ActivityType.AUTHOR_REQUEST)
                .metadata(Map.of(
                        "actionBy", login.getEmail(),
                        "affectedUserEmail", login.getEmail()
                ))
                .internal(true)
                .build());

        return ResponseEntity.ok().build();
    }

    private boolean isValidRequest(String email) {
        Optional<AuthorRequest> persistedRequest = authorRequestRepository.getAuthorRequestsByLoginEmail(email);

        if (persistedRequest.isPresent()) {
            return false;
        }

        Optional<Login> loginOptional = loginRepository.findByEmail(email);

        if (loginOptional.isEmpty()) {
            return false;
        }

        Optional<Author> authorOptional = authorRepository.findAuthorByLogin(loginOptional.get());

        return authorOptional.isEmpty() && loginOptional.get().getRole() != Role.AUTHOR;
    }
}
