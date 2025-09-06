package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @NotBlank
    @Size(max = 10000)
    private String content;

    @Min(0)
    @Max(5)
    private Integer rating;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime reviewDate;

    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
}
