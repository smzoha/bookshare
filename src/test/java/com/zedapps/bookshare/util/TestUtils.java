package com.zedapps.bookshare.util;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.book.Author;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.entity.login.ShelvedBook;
import com.zedapps.bookshare.enums.*;

import java.util.Collections;
import java.util.Set;

/**
 * @author smzoha
 * @since 26/4/26
 **/
public class TestUtils {

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

    public static Author getAuthor(String firstName, String lastName) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);

        return author;
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
                .build();
    }
}
