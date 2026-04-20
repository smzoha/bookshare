package com.zedapps.bookshare.service.shelf;

import com.zedapps.bookshare.entity.login.Shelf;
import com.zedapps.bookshare.enums.ActivityType;
import com.zedapps.bookshare.repository.login.ShelfRepository;
import com.zedapps.bookshare.service.activity.ActivityService;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shelf-lists", key = "#email")
    public List<Shelf> getShelvesForCollection(String email) {
        return shelfRepository.getShelvesForCollection(email);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "shelves", key = "#id")
    public Shelf getShelfById(Long id) {
        return shelfRepository.findById(id).orElseThrow(NoResultException::new);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "shelf-lists", key = "#shelf.user.email"),
            @CacheEvict(cacheNames = "shelves", key = "#shelf.id", condition = "#shelf.id != null")
    })
    public void saveShelf(Shelf shelf) {
        shelf = shelfRepository.save(shelf);

        activityService.saveActivityOutbox(ActivityType.SHELF_ADD,
                shelf.getId(),
                Map.of(
                        "actionBy", shelf.getUser().getEmail(),
                        "shelfId", shelf.getId(),
                        "shelfName", shelf.getName()
                ));
    }
}
