package com.zedapps.bookshare.repository.connection;

import com.zedapps.bookshare.entity.login.FriendRequest;
import com.zedapps.bookshare.entity.login.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author smzoha
 * @since 5/3/26
 **/
@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    @Query("FROM FriendRequest WHERE person1 = :personA AND person2 = :personB")
    Optional<FriendRequest> findFriendRequest(Login personA, Login personB);
}
