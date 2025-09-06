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
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq")
    @SequenceGenerator(name = "review_seq", sequenceName = "review_sequence", allocationSize = 1)
    private Long id;

    private String content;

    private Integer rating;

    private LocalDateTime reviewDate;

    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
}
