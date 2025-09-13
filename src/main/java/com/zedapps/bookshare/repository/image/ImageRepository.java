package com.zedapps.bookshare.repository.image;

import com.zedapps.bookshare.entity.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 13/9/25
 **/
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
