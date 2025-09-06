package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 7/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelvedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shelved_book_seq")
    @SequenceGenerator(name = "shelved_book_seq", sequenceName = "shelved_book_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Login login;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    private Long pagesRead;

    private LocalDateTime shelvedAt;

    private LocalDateTime updatedAt;
}
