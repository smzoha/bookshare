package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 7/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime shelvedAt;

    @Override
    public String toString() {
        return "ShelvedBook{" +
                "id=" + id +
                ", login=" + login.getId() +
                ", book=" + book.getId() +
                ", shelf=" + shelf.getId() +
                ", shelvedAt=" + shelvedAt +
                '}';
    }
}
