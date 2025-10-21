package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author smzoha
 * @since 21/10/25
 **/
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findTagByName(String name);
}
