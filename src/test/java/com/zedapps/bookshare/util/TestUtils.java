package com.zedapps.bookshare.util;

import com.zedapps.bookshare.dto.login.LoginManageDto;
import com.zedapps.bookshare.dto.login.RegistrationRequestDto;
import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.book.Genre;
import com.zedapps.bookshare.entity.book.Tag;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.entity.login.ShelvedBook;
import com.zedapps.bookshare.enums.*;
import com.zedapps.bookshare.service.auth.LoginDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * @author smzoha
 * @since 26/4/26
 **/
public class TestUtils {

    public static final List<String> TEST_ISBN_DATA = List.of(
            "0134685997",
            "9780134685991",
            "0201633612",
            "9780201633610",
            "0132350882",
            "9780132350884",
            "0596009208",
            "9780596009205",
            "0134494164",
            "9780134494166",
            "0743273567",
            "9780743273565",
            "0451524934",
            "9780451524935",
            "0345391802",
            "9780345391803",
            "0060850523",
            "9780060850524",
            "0395489326",
            "9780395489321",
            "0140449116",
            "9780140449112",
            "0553103547",
            "9780553103540",
            "0544003415"
    );

    public static Login getLogin(String email, String handle, boolean active) {
        Login login = new Login();
        login.setEmail(email);
        login.setHandle(handle);
        login.setRole(Role.USER);
        login.setFirstName("Test");
        login.setLastName("User");
        login.setActive(active);
        login.setAuthProvider(AuthProvider.LOCAL);

        return login;
    }

    public static Book getBook(String title, String isbn, Author author, Status status) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setAuthors(Set.of(author));
        book.setPages(100L);
        book.setStatus(status);

        return book;
    }

    public static List<Book> getBooks(Author author, Set<Genre> genres, Set<Tag> tags) {
        List<Book> books = new ArrayList<>();

        for (int i = 1; i <= TEST_ISBN_DATA.size(); i++) {
            String isbn = TEST_ISBN_DATA.get(i - 1);

            Book book = TestUtils.getBook("Book " + i, isbn, author, Status.ACTIVE);
            book.setId((long) i);
            book.setGenres(genres);
            book.setTags(tags);

            books.add(book);
        }

        return books;
    }

    public static Author getAuthor(String firstName, String lastName) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);

        return author;
    }

    public static Genre getGenre(String label) {
        Genre genre = new Genre();
        genre.setName(label);

        return genre;
    }

    public static Tag getTag(String label) {
        Tag tag = new Tag();
        tag.setName(label);

        return tag;
    }

    public static Review getReview(Book reviewedBook, Login login, int rating) {
        Review review = new Review();
        review.setBook(reviewedBook);
        review.setUser(login);
        review.setRating(rating);
        review.setContent("Review Content");

        return review;
    }

    public static Shelf getShelf(Login login, String name, boolean defaultShelf) {
        Shelf shelf = new Shelf();
        shelf.setUser(login);
        shelf.setName(name);
        shelf.setDefaultShelf(defaultShelf);

        return shelf;
    }

    public static ShelvedBook getShelvedBook(Book book, Login login, Shelf shelf) {
        ShelvedBook shelvedBook = new ShelvedBook();
        shelvedBook.setBook(book);
        shelvedBook.setLogin(login);
        shelvedBook.setShelf(shelf);

        return shelvedBook;
    }

    public static ActivityOutbox getActivityOutboxItem(ActivityStatus status) {
        return ActivityOutbox.builder()
                .eventType(ActivityType.LOGIN)
                .referenceId(1L)
                .referenceEntity("LOGIN")
                .payload(Collections.emptyMap())
                .status(status)
                .build();
    }

    public static Activity getActivity(ActivityType activityType) {
        return Activity.builder()
                .eventType(activityType)
                .referenceId(1L)
                .referenceEntity(activityType.getReferenceEntity())
                .metadata(Collections.emptyMap())
                .internal(!ActivityType.FEED_ACTIVITIES.contains(activityType))
                .build();
    }

    public static LoginDetails getLoginDetails(String email, String handle, boolean active) {
        Login login = getLogin(email, handle, active);

        return new LoginDetails(login.getEmail(),
                login.getFirstName(),
                login.getLastName(),
                login.getHandle(),
                List.of(new SimpleGrantedAuthority(login.getRole().name())),
                null,
                null);
    }

    public static void setupSecurityContext(LoginDetails loginDetails) {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(loginDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static RegistrationRequestDto getRegistrationRequestDto(Login login) {
        RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
        registrationRequestDto.setEmail(login.getEmail());
        registrationRequestDto.setHandle(login.getHandle());
        registrationRequestDto.setFirstName(login.getFirstName());
        registrationRequestDto.setLastName(login.getLastName());
        registrationRequestDto.setPassword("plain-password");
        registrationRequestDto.setConfirmPassword("plain-password");

        return registrationRequestDto;
    }

    public static LoginManageDto getLoginManageDto(Login login) {
        LoginManageDto loginManageDto = new LoginManageDto();
        loginManageDto.setEmail(login.getEmail());
        loginManageDto.setHandle(login.getHandle());
        loginManageDto.setFirstName(login.getFirstName());
        loginManageDto.setLastName(login.getLastName());
        loginManageDto.setPassword("plain-password");
        loginManageDto.setRole(login.getRole());
        loginManageDto.setActive(login.isActive());

        return loginManageDto;
    }
}
