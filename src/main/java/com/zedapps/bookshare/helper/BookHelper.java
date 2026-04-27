package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.dto.book.BookReviewDto;
import com.zedapps.bookshare.entity.book.Book;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.Status;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.book.BookService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;

import static com.zedapps.bookshare.entity.login.Shelf.*;

/**
 * @author smzoha
 * @since 27/4/26
 **/
@Component
@RequiredArgsConstructor
public class BookHelper {

    private final BookService bookService;
    private final LoginService loginService;
    private final ShelfService shelfService;

    @Transactional(readOnly = true)
    public void setupReferenceData(LoginDetails loginDetails, Long bookId, ModelMap model,
                                   boolean addNewReview, boolean addNewProgress) {

        Book book = bookService.getBook(bookId);
        assert book.getStatus() == Status.ACTIVE : "Book is not in active status";

        model.put("book", book);

        if (Objects.nonNull(loginDetails)) {
            Login login = loginService.getLogin(loginDetails.getEmail());

            setupShelfReferenceData(login, model, book);
            model.put("readingProgresses", login.getReadingProgresses(book.getId()));
        }

        model.put("tmpShelf", new Shelf());
        if (addNewProgress) model.put("tmpProgress", new ReadingProgress());
        if (addNewReview) model.put("reviewDto", new BookReviewDto());

        model.put("reviews", bookService.getReviewsByBook(book, 0));
        model.put("relatedBooks", bookService.getRelatedBooks(book, book.getGenres(), book.getTags()));
    }

    public void setupShelfReferenceData(Login login, ModelMap model, Book book) {
        Map<String, Shelf> defaultShelves = new HashMap<>();
        List<Shelf> otherShelves = new ArrayList<>();

        for (Shelf shelf : shelfService.getShelvesForCollection(login.getEmail())) {
            if (shelf.isDefaultShelf()) defaultShelves.put(shelf.getName(), shelf);
            else otherShelves.add(shelf);
        }

        Shelf defaultShelf = getDefaultShelf(book, defaultShelves);

        model.put("defaultShelves", defaultShelves.values());
        model.put("defaultShelf", defaultShelf);

        model.put("allShelves", otherShelves);
        model.put("shelvesTruncated", otherShelves.size() > 5);

        model.put("shelves", otherShelves.stream()
                .sorted(Comparator.comparing((Shelf s) -> s.containsBook(book)).reversed())
                .limit(5)
                .toList());
    }

    private Shelf getDefaultShelf(Book book, Map<String, Shelf> defaultShelves) {
        if (defaultShelves.containsKey(SHELF_READ) && defaultShelves.get(SHELF_READ).containsBook(book)) {
            return defaultShelves.get(SHELF_READ);

        } else if (defaultShelves.containsKey(SHELF_CURRENTLY_READING)
                && defaultShelves.get(SHELF_CURRENTLY_READING).containsBook(book)) {

            return defaultShelves.get(SHELF_CURRENTLY_READING);

        } else {
            return defaultShelves.get(SHELF_WANT_TO_READ);
        }
    }
}
