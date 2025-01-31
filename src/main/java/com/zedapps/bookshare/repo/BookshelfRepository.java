package com.zedapps.bookshare.repo;

import com.zedapps.bookshare.entity.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 31/1/25
 **/
@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {
}
