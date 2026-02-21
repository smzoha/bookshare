package com.zedapps.bookshare.repository.activity;

import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import com.zedapps.bookshare.entity.activity.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Repository
public interface ActivityOutboxRepository extends JpaRepository<ActivityOutbox, Long> {

    List<ActivityOutbox> findTop100ByStatusOrderByCreatedAt(ActivityStatus status);

    List<ActivityOutbox> findByStatusIn(List<ActivityStatus> statuses);
}
