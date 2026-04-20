package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.api.book.BookDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfCreateDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDetailDto;
import com.zedapps.bookshare.dto.api.shelf.ShelfDto;
import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.book.BookApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author smzoha
 * @since 20/4/26
 **/
@Service
@RequiredArgsConstructor
public class ShelfApiService {

    private final ShelfService shelfService;
    private final LoginService loginService;
    private final BookApiService bookApiService;

    public List<ShelfDto> getShelfDtoList(LoginDetails loginDetails) {
        List<Shelf> shelves = shelfService.getShelvesForCollection(loginDetails.getEmail());

        return shelves.stream()
                .map(shelf -> new ShelfDto(shelf.getName(), loginDetails.getEmail(),
                        shelf.getBooks().size(), shelf.isDefaultShelf()))
                .toList();
    }

    public Shelf getShelf(Long id) {
        return shelfService.getShelfById(id);
    }

    public ShelfDetailDto getShelfDetailDto(Shelf shelf) {
        List<BookDto> bookDtoList = shelf.getBooks().stream()
                .map(sb -> bookApiService.createDto(sb.getBook(), false))
                .toList();

        return new ShelfDetailDto(shelf.getName(), shelf.getUser().getEmail(), shelf.isDefaultShelf(), bookDtoList);
    }

    @Transactional
    public ShelfDto saveShelf(ShelfCreateDto shelfCreateDto, LoginDetails loginDetails) {
        Login login = loginService.getLogin(loginDetails.getEmail());
        Shelf shelf = new Shelf(shelfCreateDto.name(), login);

        shelfService.saveShelf(shelf);

        return new ShelfDto(shelfCreateDto.name(), loginDetails.getEmail(), 0, false);
    }
}
