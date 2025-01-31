package com.zedapps.bookshare.repo;

import com.zedapps.bookshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 31/1/25
 **/
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
