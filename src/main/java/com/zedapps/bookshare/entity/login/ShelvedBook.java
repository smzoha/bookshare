package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Login login;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    @Min(value = 0, message = "{error.min.value}")
    private Long pagesRead = 0L;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime shelvedAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
