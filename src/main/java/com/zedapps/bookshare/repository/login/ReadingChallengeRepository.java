package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.ReadingChallenge;
import com.zedapps.bookshare.entity.login.ReadingChallengeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 17/6/26
 **/
@Repository
public interface ReadingChallengeRepository extends JpaRepository<ReadingChallenge, ReadingChallengeId> {
}
