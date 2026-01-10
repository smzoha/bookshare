package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelf {

    public static String SHELF_READ = "Read";
    public static String SHELF_CURRENTLY_READING = "Currently Reading";
    public static String SHELF_WANT_TO_READ = "Want to Read";

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
    private List<ShelvedBook> books = new ArrayList<>();

    private boolean defaultShelf;

    public boolean containsBook(Book book) {
        if (CollectionUtils.isEmpty(books)) {
            return false;
        }

        return books.stream().anyMatch(b -> Objects.equals(b.getBook(), book));
    }
}

