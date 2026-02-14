package com.zedapps.bookshare.service.book;

import com.zedapps.bookshare.dto.login.LoginDetails;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author smzoha
 * @since 15/2/26
 **/
@Service
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final ActivityService activityService;

    public ShelfService(ShelfRepository shelfRepository, ActivityService activityService) {
        this.shelfRepository = shelfRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Shelf saveShelf(Shelf shelf, LoginDetails loginDetails) {
        shelf = shelfRepository.save(shelf);

        activityService.saveActivityOutbox(ActivityType.SHELF_ADD,
                shelf.getId(),
                Map.of(
                        "actionBy", loginDetails.getEmail(),
                        "shelfId", shelf.getId(),
                        "shelfName", shelf.getName()
                ));

        return shelf;
    }
}
