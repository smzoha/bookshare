package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.FriendRequest;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.ReadingProgress;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.enums.ConnectionAction;
import com.zedapps.bookshare.repository.login.ConnectionRepository;
import com.zedapps.bookshare.repository.login.FriendRequestRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author smzoha
 * @since 28/2/26
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FriendRequestRepository friendRequestRepository;
    private final ConnectionRepository connectionRepository;
    private final ActivityService activityService;

    @Transactional(readOnly = true)
    public List<Connection> getConnectionsByPerson(Login person1) {
        return connectionRepository.findConnectionsByPerson1(person1);
    }

    @Transactional(readOnly = true)
    public Optional<FriendRequest> getFriendRequest(Login person1, Login person2) {
        return friendRequestRepository.findFriendRequest(person1, person2);
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

    public List<ReadingProgress> getDistinctReadingProgressList(Login login) {
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
                .sorted(Comparator.comparing(ReadingProgress::getStartDate).reversed()
                        .thenComparing(rp -> rp.getBook().getTitle()))
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
