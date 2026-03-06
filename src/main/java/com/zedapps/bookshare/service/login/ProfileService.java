package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.*;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.repository.connection.ConnectionRepository;
import com.zedapps.bookshare.repository.connection.FriendRequestRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
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
    private final ActivityService activityService;

    public void setupReferenceData(String profileEmail, LoginDetails loginDetails, ModelMap model) {
        Login profileLogin = loginService.getLogin(profileEmail);
        Login authLogin = loginService.getLogin(loginDetails.getEmail());

        model.put("login", profileLogin);
        model.put("totalBooks", profileLogin.getShelves()
                .stream()
                .mapToInt(shelf -> shelf.getBooks().size())
                .sum());

        setupShelves(model, profileLogin);

        model.put("readingProgressList", getDistinctReadingProgressList(profileLogin));

        List<Connection> connections = connectionRepository.findConnectionsByPerson1(profileLogin);
        model.put("connectionsCount", connections.size());
        model.put("connections", connections);

        setupConnectionRefData(model, profileLogin, authLogin);
    }

    public void setupConnectionRefData(ModelMap model, Login profileLogin, Login authLogin) {
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
    public void performConnectionAction(Login authLogin, Login profileLogin, ConnectionAction action) {
        switch (action) {
            case SEND_FRIEND_REQ:
                saveFriendRequest(authLogin, profileLogin);
                break;

            case REVOKE_FRIEND_REQ:
                revokeFriendRequest(authLogin, profileLogin);
                break;

            case ACCEPT_FRIEND_REQ:
                acceptFriendRequest(authLogin, profileLogin);
                break;

            case DECLINE_FRIEND_REQ:
                declineFriendRequest(authLogin, profileLogin);
                break;

            case REMOVE_FRIEND:
                removeFriend(authLogin, profileLogin);
                break;
        }
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

    private void saveFriendRequest(Login authLogin, Login profileLogin) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setPerson1(authLogin);
        friendRequest.setPerson2(profileLogin);

        friendRequest = friendRequestRepository.save(friendRequest);

        activityService.saveActivityOutbox(ActivityType.FRIEND_REQ_SENT,
                friendRequest.getId(),
                Map.of(
                        "actionBy", authLogin.getEmail(),
                        "requestedBy", authLogin.getId(),
                        "requestedTo", profileLogin.getId(),
                        "requestedByEmail", authLogin.getEmail(),
                        "requestedToEmail", profileLogin.getEmail()
                ));
    }

    private void revokeFriendRequest(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(authLogin, profileLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());

        activityService.saveActivityOutbox(ActivityType.REVOKE_FRIEND_REQ,
                request.get().getId(),
                Map.of(
                        "actionBy", authLogin.getEmail(),
                        "requestedBy", authLogin.getId(),
                        "requestedTo", profileLogin.getId(),
                        "requestedByEmail", authLogin.getEmail(),
                        "requestedToEmail", profileLogin.getEmail()
                ));
    }

    private void acceptFriendRequest(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(profileLogin, authLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());

        Connection connection = connectionRepository.save(new Connection(authLogin, profileLogin));
        Connection reverseConnection = connectionRepository.save(new Connection(profileLogin, authLogin));

        activityService.saveActivityOutbox(ActivityType.ADD_FRIEND,
                connection.getId(),
                Map.of(
                        "actionBy", authLogin.getEmail(),
                        "acceptedBy", authLogin.getId(),
                        "requestFrom", profileLogin.getId(),
                        "acceptedByEmail", authLogin.getEmail(),
                        "requestFromEmail", profileLogin.getEmail(),
                        "reverseConnection", reverseConnection.getId()
                ));
    }

    private void declineFriendRequest(Login authLogin, Login profileLogin) {
        Optional<FriendRequest> request = friendRequestRepository.findFriendRequest(profileLogin, authLogin);

        if (logInvalidRequest(authLogin, profileLogin, request)) return;

        friendRequestRepository.delete(request.get());

        activityService.saveActivityOutbox(ActivityType.DECLINE_FRIEND_REQ,
                request.get().getId(),
                Map.of(
                        "actionBy", authLogin.getEmail(),
                        "declinedBy", authLogin.getId(),
                        "requestBy", profileLogin.getId(),
                        "declinedByEmail", authLogin.getEmail(),
                        "requestByEmail", profileLogin.getEmail()
                ));
    }

    private void removeFriend(Login authLogin, Login profileLogin) {
        Connection connection = connectionRepository.findConnectionByPerson1AndPerson2(authLogin, profileLogin);
        Connection reverseConnection = connectionRepository.findConnectionByPerson1AndPerson2(profileLogin, authLogin);

        connectionRepository.delete(connection);
        connectionRepository.delete(reverseConnection);

        activityService.saveActivityOutbox(ActivityType.REMOVE_FRIEND,
                connection.getId(),
                Map.of(
                        "actionBy", authLogin.getEmail(),
                        "removedBy", authLogin.getId(),
                        "removedFrom", profileLogin.getId(),
                        "removedByEmail", authLogin.getEmail(),
                        "removedFromEmail", profileLogin.getEmail(),
                        "reverseConnection", reverseConnection.getId()
                ));
    }

    private boolean logInvalidRequest(Login authLogin, Login profileLogin, Optional<FriendRequest> request) {
        if (request.isEmpty()) {
            log.error("Friend request does not exist. personA: {}, personB: {}", authLogin.getEmail(), profileLogin.getEmail());
            return true;
        }

        return false;
    }
}
