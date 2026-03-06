package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.repository.connection.ConnectionRepository;
import com.zedapps.bookshare.repository.connection.FriendRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author smzoha
 * @since 28/2/26
 **/
@Slf4j
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
        model.put("readingProgressList", getDistinctReadingProgressList(profileLogin));

        setupFriendFlags(model, profileLogin, authLogin);
    }

    public void setupFriendFlags(ModelMap model, Login profileLogin, Login authLogin) {
        boolean friendReqSent = friendRequestRepository.findFriendRequest(profileLogin, authLogin).isPresent();
        boolean friendReqReceived = friendRequestRepository.findFriendRequest(authLogin, profileLogin).isPresent();

        List<Connection> connections = connectionRepository.findConnectionsByPerson1(authLogin);
        boolean isFriends = connections.stream().anyMatch(conn -> conn.getPerson2().equals(profileLogin));

        model.put("ownProfile", Objects.equals(profileLogin, authLogin));

        model.put("friendReqReceived", friendReqReceived);
        model.put("friendReqSent", friendReqSent);

        model.put("isFriends", isFriends);
        model.put("showFriendReqBtn", !friendReqSent && !friendReqReceived && !isFriends);
    }

    @Transactional
    public void saveFriendRequest(Login authLogin, Login profileLogin) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setPerson1(authLogin);
        friendRequest.setPerson2(profileLogin);

        friendRequestRepository.save(friendRequest);
    }

    @Transactional
    public void addFriend(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(profileLogin, authLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());
        connectionRepository.saveConnection(authLogin.getId(), profileLogin.getId());
    }

    @Transactional
    public void declineFriendRequest(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(profileLogin, authLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());
    }

    @Transactional
    public void revokeFriendRequest(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(authLogin, profileLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());
    }

    @Transactional
    public void removeFriend(Login authLogin, Login profileLogin) {
        connectionRepository.deleteConnection(authLogin.getId(), profileLogin.getId());
        connectionRepository.deleteConnection(profileLogin.getId(), authLogin.getId());
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

    private boolean logInvalidRequest(Login authLogin, Login profileLogin, Optional<FriendRequest> request) {
        if (request.isEmpty()) {
            log.error("Friend request does not exist. personA: {}, personB: {}", authLogin.getEmail(), profileLogin.getEmail());
            return true;
        }

        return false;
    }
}
