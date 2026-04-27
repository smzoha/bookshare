package com.zedapps.bookshare.helper;

import com.zedapps.bookshare.dto.feed.FeedDto;
import com.zedapps.bookshare.entity.feed.FeedEntry;
import com.zedapps.bookshare.entity.login.Connection;
import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.service.auth.LoginDetails;
import com.zedapps.bookshare.service.login.FeedService;
import com.zedapps.bookshare.service.login.LoginService;
import com.zedapps.bookshare.service.login.ProfileService;
import com.zedapps.bookshare.service.shelf.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author smzoha
 * @since 27/4/26
 **/
@Component
@RequiredArgsConstructor
public class ProfileHelper {

    private final ProfileService profileService;
    private final LoginService loginService;
    private final ShelfService shelfService;
    private final FeedService feedService;

    public void setupReferenceData(String profileEmail, LoginDetails loginDetails, ModelMap model) {
        Login profileLogin = loginService.getLogin(profileEmail);
        Login authLogin = loginService.getLogin(loginDetails.getEmail());

        model.put("login", profileLogin);

        List<Shelf> shelves = shelfService.getShelvesForCollection(profileEmail);

        model.put("totalBooks", shelves
                .stream()
                .mapToInt(shelf -> shelf.getBooks().size())
                .sum());

        setupShelves(model, shelves);

        model.put("readingProgressList", profileService.getDistinctReadingProgressList(profileLogin));

        List<Connection> connections = profileService.getConnectionsByPerson(profileLogin);
        model.put("connectionsCount", connections.size());
        model.put("connections", connections);

        setupConnectionRefData(model, profileLogin, authLogin);

        Page<FeedEntry> feedEntries = feedService.getFeedEntries(profileLogin, 5, 0);
        List<FeedDto> feedDtoList = feedService.mapToFeedDtoList(feedEntries);
        model.put("feedDtoList", feedDtoList);
    }

    public void setupConnectionRefData(ModelMap model, Login profileLogin, Login authLogin) {
        boolean friendReqSent = profileService.getFriendRequest(profileLogin, authLogin).isPresent();
        boolean friendReqReceived = profileService.getFriendRequest(authLogin, profileLogin).isPresent();

        List<Connection> connections = profileService.getConnectionsByPerson(authLogin);
        boolean isFriends = connections.stream().anyMatch(conn -> conn.getPerson2().equals(profileLogin));

        model.put("ownProfile", Objects.equals(profileLogin, authLogin));

        model.put("friendReqReceived", friendReqReceived);
        model.put("friendReqSent", friendReqSent);

        model.put("isFriends", isFriends);
        model.put("showFriendReqBtn", !friendReqSent && !friendReqReceived && !isFriends);
    }

    private void setupShelves(ModelMap model, List<Shelf> shelves) {
        Map<Long, String> defaultShelves = new LinkedHashMap<>();
        Map<Long, String> shelfMap = new LinkedHashMap<>();
        Shelf activeShelf = null;

        for (Shelf shelf : shelves) {
            if (shelf.isDefaultShelf()) {
                defaultShelves.put(shelf.getId(), shelf.getName());
                if (activeShelf == null) activeShelf = shelf;

            } else {
                shelfMap.put(shelf.getId(), shelf.getName());
            }
        }

        model.put("defaultShelves", defaultShelves);
        model.put("shelves", shelfMap);
        model.put("activeShelf", activeShelf);
    }
}
