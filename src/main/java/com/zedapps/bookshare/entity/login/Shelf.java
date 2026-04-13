package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "shelf.withBooks",
        attributeNodes = @NamedAttributeNode(value = "books", subgraph = "shelved-book-subgraph"),
        subgraphs = @NamedSubgraph(
                name = "shelved-book-subgraph",
                attributeNodes = @NamedAttributeNode("book")
        )
)
public class Shelf {

    public final static String SHELF_READ = "Read";
    public final static String SHELF_CURRENTLY_READING = "Currently Reading";
    public final static String SHELF_WANT_TO_READ = "Want to Read";

    public static final List<String> DEFAULT_SHELF_NAMES = List.of(
            SHELF_CURRENTLY_READING,
            SHELF_WANT_TO_READ,
            SHELF_READ);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shelf_seq")
    @SequenceGenerator(name = "shelf_seq", sequenceName = "shelf_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String name;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login user;

    @OneToMany(mappedBy = "shelf")
    private Set<ShelvedBook> books = new LinkedHashSet<>();

    @Column(updatable = false)
    private boolean defaultShelf;

    public Shelf(String name, Login user) {
        this.name = name;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shelf other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Shelf{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", defaultShelf=" + defaultShelf +
                '}';
    }

    public boolean containsBook(Book book) {
        if (CollectionUtils.isEmpty(books)) {
            return false;
        }

        return books.stream().anyMatch(b -> Objects.equals(b.getBook(), book));
    }
}
