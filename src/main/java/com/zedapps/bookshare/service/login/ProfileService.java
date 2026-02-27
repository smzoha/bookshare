package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Shelf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author smzoha
 * @since 28/2/26
 **/
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final LoginService loginService;

    public void setupReferenceData(LoginDetails loginDetails, ModelMap model) {
        Login login = loginService.getLogin(loginDetails.getEmail());

        model.put("login", login);
        model.put("totalBooks", login.getShelves()
                .stream()
                .mapToInt(shelf -> shelf.getBooks().size())
                .sum());

        setupShelves(model, login);

        model.put("readingProgressList", getDistinctReadingProgressList(login));
    }

    private void setupShelves(ModelMap model, Login login) {
        Map<Long, String> defaultShelves = new LinkedHashMap<>();
        Map<Long, String> shelves = new LinkedHashMap<>();
        Shelf activeShelf = null;

        for (Shelf shelf : login.getShelves()) {
            if (shelf.isDefaultShelf()) {
                defaultShelves.put(shelf.getId(), shelf.getName());
                if (activeShelf == null) activeShelf = shelf;

            } else {
                shelves.put(shelf.getId(), shelf.getName());
            }
        }

        model.put("defaultShelves", defaultShelves);
        model.put("shelves", shelves);
        model.put("activeShelf", activeShelf);
    }

    private List<ReadingProgress> getDistinctReadingProgressList(Login login) {
        return login.getReadingProgresses()
                .stream()
                .filter(rp -> !rp.isCompleted())
                .collect(Collectors.toMap(
                        ReadingProgress::getBook,
                        rp -> rp,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .limit(5)
                .toList();
    }
}
