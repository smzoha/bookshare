package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * @author smzoha
 * @since 24/4/26
 **/
@Service
@RequiredArgsConstructor
public class AuthorRequestService {

    private final AuthorRequestRepository authorRequestRepository;
    private final LoginRepository loginRepository;
    private final AuthorRepository authorRepository;
    private final ApplicationEventPublisher publisher;

    public Login getValidLoginForRequest(String email) {
        Optional<AuthorRequest> persistedRequest = authorRequestRepository.getAuthorRequestsByLoginEmail(email);

        if (persistedRequest.isPresent()) {
            return null;
        }

        Optional<Login> loginOptional = loginRepository.findByEmail(email);

        if (loginOptional.isEmpty()) {
            return null;
        }

        Optional<Author> authorOptional = authorRepository.findAuthorByLogin(loginOptional.get());

        return authorOptional.isEmpty() && loginOptional.get().getRole() != Role.AUTHOR
                ? loginOptional.get()
                : null;
    }

    @Transactional
    public void saveAuthorRequest(Login login) {
        authorRequestRepository.save(new AuthorRequest(login));

        publisher.publishEvent(ActivityEvent.builder()
                .loginEmail(login.getEmail())
                .eventType(ActivityType.AUTHOR_REQUEST)
                .metadata(Map.of(
                        "actionBy", login.getEmail(),
                        "affectedUserEmail", login.getEmail()
                ))
                .internal(true)
                .build());
    }
}
