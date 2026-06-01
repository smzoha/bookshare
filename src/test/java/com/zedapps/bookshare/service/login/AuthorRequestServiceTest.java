package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.activity.ActivityEvent;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.AuthorRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.Role;
import com.zedapps.bookshare.repository.book.AuthorRepository;
import com.zedapps.bookshare.repository.book.AuthorRequestRepository;
import com.zedapps.bookshare.repository.login.LoginRepository;
import com.zedapps.bookshare.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 20/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class AuthorRequestServiceTest {

    @InjectMocks
    private AuthorRequestService authorRequestService;

    @Mock
    private AuthorRequestRepository authorRequestRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    private Login login;
    private Author author;
    private AuthorRequest authorRequest;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);

        author = TestUtils.getAuthor("Test", "User");
        author.setLogin(login);
        author.setId(1L);

        authorRequest = new AuthorRequest();
        authorRequest.setLogin(login);
        authorRequest.setId(1L);

        lenient().when(authorRequestRepository.getAuthorRequestsByLoginEmail(login.getEmail()))
                .thenReturn(Optional.empty());

        lenient().when(loginRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(login));
        lenient().when(authorRepository.findAuthorByLogin(login)).thenReturn(Optional.empty());
    }

    @Test
    void getValidLoginForRequest_existingAuthorRequest_returnsNull() {
        when(authorRequestRepository.getAuthorRequestsByLoginEmail(eq(login.getEmail())))
                .thenReturn(Optional.of(authorRequest));

        Login validLogin = authorRequestService.getValidLoginForRequest(login.getEmail());

        verify(authorRequestRepository).getAuthorRequestsByLoginEmail(eq(login.getEmail()));
        verifyNoInteractions(loginRepository, authorRepository);
        assertNull(validLogin);
    }

    @Test
    void getValidLoginForRequest_loginNotFound_returnsNull() {
        when(loginRepository.findByEmail(eq(login.getEmail()))).thenReturn(Optional.empty());

        Login validLogin = authorRequestService.getValidLoginForRequest(login.getEmail());

        verify(authorRequestRepository).getAuthorRequestsByLoginEmail(eq(login.getEmail()));
        verify(loginRepository).findByEmail(eq(login.getEmail()));
        verifyNoInteractions(authorRepository);

        assertNull(validLogin);
    }

    @Test
    void getValidLoginForRequest_loginAlreadyAnAuthor_returnsNull() {
        when(authorRepository.findAuthorByLogin(login)).thenReturn(Optional.of(author));

        Login validLogin = authorRequestService.getValidLoginForRequest(login.getEmail());

        verify(authorRequestRepository).getAuthorRequestsByLoginEmail(eq(login.getEmail()));
        verify(loginRepository).findByEmail(eq(login.getEmail()));
        verify(authorRepository).findAuthorByLogin(login);

        assertNull(validLogin);
    }

    @Test
    void getValidLoginForRequest_loginHasAuthorRole_returnsNull() {
        login.setRole(Role.AUTHOR);
        Login validLogin = authorRequestService.getValidLoginForRequest(login.getEmail());

        verify(authorRequestRepository).getAuthorRequestsByLoginEmail(eq(login.getEmail()));
        verify(loginRepository).findByEmail(eq(login.getEmail()));
        verify(authorRepository).findAuthorByLogin(login);


        assertNull(validLogin);
    }

    @Test
    void getValidLoginForRequest_allChecksPass_returnsLogin() {
        Login validLogin = authorRequestService.getValidLoginForRequest(login.getEmail());

        verify(authorRequestRepository).getAuthorRequestsByLoginEmail(eq(login.getEmail()));
        verify(loginRepository).findByEmail(eq(login.getEmail()));
        verify(authorRepository).findAuthorByLogin(login);

        assertEquals(login, validLogin);
    }

    @Test
    void saveAuthorRequest_validLogin_persistsRequest() {
        authorRequestService.saveAuthorRequest(login);
        verify(authorRequestRepository).save(any(AuthorRequest.class));
    }

    @Test
    void saveAuthorRequest_validLogin_publishesAuthorRequestActivityEvent() {
        authorRequestService.saveAuthorRequest(login);

        verify(publisher).publishEvent(eq(ActivityEvent.builder()
                .loginEmail(login.getEmail())
                .eventType(ActivityType.AUTHOR_REQUEST)
                .metadata(Map.of("actionBy", login.getEmail(),
                        "affectedUserEmail", login.getEmail()))
                .internal(true)
                .build()));
    }
}
