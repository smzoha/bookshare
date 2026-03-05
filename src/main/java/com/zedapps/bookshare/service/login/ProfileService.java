package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.connection.ConnectionRepository;
import com.zedapps.bookshare.repository.connection.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author smzoha
 * @since 28/2/26
 **/
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final LoginService loginService;
    private final FriendRequestRepository friendRequestRepository;
    private final ConnectionRepository connectionRepository;

    public void setupReferenceData(String profileEmail, LoginDetails loginDetails, ModelMap model) {
        Login profileLogin = loginService.getLogin(profileEmail);
        Login authLogin = loginService.getLogin(loginDetails.getEmail());

        model.put("login", profileLogin);
        model.put("totalBooks", profileLogin.getShelves()
                .stream()
                .mapToInt(shelf -> shelf.getBooks().size())
                .sum());

        setupShelves(model, profileLogin);

        model.put("connectionsCount", connectionRepository.findConnectionsByPerson1(profileLogin).size());
        model.put("ownProfile", Objects.equals(profileLogin, authLogin));
        model.put("readingProgressList", getDistinctReadingProgressList(profileLogin));

        setupFriendFlags(model, profileLogin, authLogin);
    }

    private void setupFriendFlags(ModelMap model, Login profileLogin, Login authLogin) {
        boolean friendReqReceived = friendRequestRepository.findFriendRequest(profileLogin, authLogin).isPresent();
        boolean friendReqSent = friendRequestRepository.findFriendRequest(authLogin, profileLogin).isPresent();

        List<Connection> connections = connectionRepository.findConnectionsByPerson1(authLogin);
        boolean isFriends = connections.stream().anyMatch(conn -> conn.getPerson2().equals(profileLogin));

        model.put("friendReqReceived", friendReqReceived);
        model.put("friendReqSent", friendReqSent);

        model.put("isFriends", isFriends);
        model.put("showFriendReqBtn", !friendReqReceived && !friendReqSent && !isFriends);
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
