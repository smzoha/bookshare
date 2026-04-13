package com.zedapps.bookshare.repository.login;

import com.zedapps.bookshare.entity.login.Shelf;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author smzoha
 * @since 12/12/25
 **/
@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    @EntityGraph("shelf.withBooks")
    @Query("FROM Shelf WHERE user.email = :email ORDER BY defaultShelf DESC, name")
    List<Shelf> getShelvesForCollection(String email);

    @EntityGraph("shelf.withBooks")
    Optional<Shelf> findById(Long id);
}
