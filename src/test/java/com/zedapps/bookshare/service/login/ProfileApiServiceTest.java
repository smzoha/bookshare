package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.book.ReadingProgressDto;
import com.zedapps.bookshare.dto.api.login.ConnectionApiDto;
import com.zedapps.bookshare.dto.api.login.LoginApiDto;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.repository.login.ConnectionRepository;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.util.TestUtils;
import com.zedapps.bookshare.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author smzoha
 * @since 20/5/26
 **/
@ExtendWith(MockitoExtension.class)
public class ProfileApiServiceTest {

    @InjectMocks
    private ProfileApiService profileApiService;

    @Mock
    private LoginService loginService;

    @Mock
    private ConnectionRepository connectionRepository;

    @Mock
    private ProfileService profileService;

    private Login login;
    private Login friendLogin;
    private LoginDetails loginDetails;

    private Image image;

    @BeforeEach
    void setUp() {
        login = TestUtils.getLogin("test@test.com", "test", true);
        login.setId(1L);
        login.setShelves(Collections.emptySet());

        friendLogin = TestUtils.getLogin("friend@test.com", "friend", true);
        friendLogin.setId(2L);

        image = new Image(1L, "image.png", "image/png", null, LocalDateTime.now());
        login.setProfilePicture(image);

        loginDetails = TestUtils.getLoginDetails(login.getEmail(), login.getHandle(), true);

        lenient().when(loginService.getLoginByHandle(login.getHandle())).thenReturn(login);
    }
    
    @Test
    void getLogin_notDetailed_returnsBasicDto() {
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.getImageUrl(image))
                    .thenReturn("http://localhost/image/" + image.getId());

            LoginApiDto loginApiDto = profileApiService.getLogin(login.getHandle(), false);

            assertNotNull(loginApiDto);
            assertEquals(login.getFirstName(), loginApiDto.firstName());
            assertEquals(login.getLastName(), loginApiDto.lastName());
            assertEquals(login.getEmail(), loginApiDto.email());
            assertEquals(login.getHandle(), loginApiDto.handle());
            assertTrue(loginApiDto.profilePictureUrl().endsWith("/image/" + image.getId()));

            assertNull(loginApiDto.shelfList());
            assertNull(loginApiDto.connectionList());
            assertNull(loginApiDto.readingProgress());
        }
    }

    @Test
    void getLogin_detailed_includesConnectionsAndReadingProgress() {
        setupLoginAssociatedListStubs();

        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.getImageUrl(image))
                    .thenReturn("http://localhost/image/" + image.getId());

            mockedUtils.when(() -> Utils.getImageUrl(null)).thenReturn("");

            LoginApiDto loginApiDto = profileApiService.getLogin(login.getHandle(), true);

            assertNotNull(loginApiDto);
            assertTrue(loginApiDto.shelfList().isEmpty());

            assertFalse(loginApiDto.connectionList().isEmpty());

            LoginApiDto friend = loginApiDto.connectionList().getFirst();
            assertEquals(friendLogin.getEmail(), friend.email());

            assertFalse(loginApiDto.readingProgress().isEmpty());

            ReadingProgressDto readingProgressDto = loginApiDto.readingProgress().getFirst();
            assertEquals("Book 1", readingProgressDto.bookTitle());
            assertEquals(5L, readingProgressDto.pagesRead());
            assertFalse(readingProgressDto.completed());
        }
    }

    @Test
    void getLogin_nullImageUrl_mapsToEmptyString() {
        login.setProfilePicture(null);

        LoginApiDto loginApiDto = profileApiService.getLogin(login.getHandle(), false);

        assertNotNull(loginApiDto);
        assertTrue(loginApiDto.profilePictureUrl().isEmpty());
    }

    @Test
    void performConnectionAction_validRequest_delegatesToProfileService() {
        ConnectionApiDto connectionApiDto = new ConnectionApiDto("friend", ConnectionAction.SEND_FRIEND_REQ);

        when(loginService.getLogin(loginDetails.getEmail())).thenReturn(login);
        when(loginService.getLoginByHandle(friendLogin.getHandle())).thenReturn(friendLogin);

        profileApiService.performConnectionAction(loginDetails, connectionApiDto);

        verify(profileService).performConnectionAction(login, friendLogin, connectionApiDto.action());
    }

    private void setupLoginAssociatedListStubs() {
        Author author = TestUtils.getAuthor("Test", "Author");
        author.setId(1L);

        Book book = TestUtils.getBook("Book 1", "9780743273565", author, Status.ACTIVE);
        book.setId(1L);

        ReadingProgress readingProgress = new ReadingProgress(1L, login, book, 5L,
                LocalDate.now(), null, false, LocalDateTime.now());

        when(profileService.getDistinctReadingProgressList(login)).thenReturn(List.of(readingProgress));

        Connection connection = new Connection();
        connection.setId(1L);
        connection.setPerson1(login);
        connection.setPerson2(friendLogin);

        when(connectionRepository.findConnectionsByPerson1(login)).thenReturn(List.of(connection));
    }
}
