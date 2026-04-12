package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author smzoha
 * @since 7/9/25
 **/
@Entity
@Getter
@Setter
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShelvedBook other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

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
