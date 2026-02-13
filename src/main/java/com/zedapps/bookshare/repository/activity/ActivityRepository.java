package com.zedapps.bookshare.repository.activity;

import com.zedapps.bookshare.entity.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 13/2/26
 **/
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
