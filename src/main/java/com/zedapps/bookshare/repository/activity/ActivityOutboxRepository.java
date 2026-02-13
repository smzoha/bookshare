package com.zedapps.bookshare.repository.activity;

import com.zedapps.bookshare.entity.activity.ActivityOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Repository
public interface ActivityOutboxRepository extends JpaRepository<ActivityOutbox, Long> {
}
