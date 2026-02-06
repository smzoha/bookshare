package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author smzoha
 * @since 12/12/25
 **/
@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    List<Shelf> findAllByUser_EmailOrderByName(String userEmail);
}
