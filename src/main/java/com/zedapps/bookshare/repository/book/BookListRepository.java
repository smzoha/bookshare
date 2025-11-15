package com.zedapps.bookshare.repository.book;

import com.zedapps.bookshare.entity.book.Book;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * @author smzoha
 * @since 15/11/25
 **/
@Repository
public interface BookListRepository extends PagingAndSortingRepository<Book, Long> {
}
