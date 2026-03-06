package com.zedapps.bookshare.service.login;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author smzoha
 * @since 15/2/26
 **/
@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final ActivityService activityService;

    @Transactional
    public void saveShelf(Shelf shelf, LoginDetails loginDetails) {
        shelf = shelfRepository.save(shelf);

        activityService.saveActivityOutbox(ActivityType.SHELF_ADD,
                shelf.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "shelfId", shelf.getId(),
                        "shelfName", shelf.getName()
                ));
    }
}
