package com.zedapps.bookshare.entity;

import com.zedapps.bookshare.entity.enums.ShelfName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 31/1/25
 * Generated via ChatGPT
 **/
@Entity
@Table(name = "bookshelves")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bookshelf {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookshelf_id_seq")
    @SequenceGenerator(name = "bookshelf_id_seq", sequenceName = "bookshelf_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    private ShelfName shelfName;

    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        addedAt = LocalDateTime.now();
    }
}
