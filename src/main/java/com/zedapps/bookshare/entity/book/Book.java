package com.zedapps.bookshare.entity.book;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.entity.login.Review;
import com.zedapps.bookshare.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        name = "book.withAssociations",
        attributeNodes = {
                @NamedAttributeNode("authors"),
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("genres"),
                @NamedAttributeNode("image")
        }
)
@NamedEntityGraph(
        name = "book.withAll",
        attributeNodes = {
                @NamedAttributeNode("authors"),
                @NamedAttributeNode("tags"),
                @NamedAttributeNode("genres"),
                @NamedAttributeNode("image"),
                @NamedAttributeNode("reviews")
        }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_seq")
    @SequenceGenerator(name = "book_seq", sequenceName = "book_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String title;

    @NotBlank(message = "{error.blank}")
    @ISBN(message = "{error.isbn}")
    private String isbn;

    private String description;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Min(value = 0, message = "{error.min.value}")
    @Max(value = 20000, message = "{error.max.value}")
    private Long pages;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "book_tags",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "book_genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new LinkedHashSet<>();

    @OneToMany(mappedBy = "book")
    @OrderBy("reviewDate DESC")
    private Set<Review> reviews = new LinkedHashSet<>();

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", pages=" + pages +
                ", publicationDate=" + publicationDate +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", image=" + image.getId() +
                '}';
    }

    public String getAuthorNames() {
        if (CollectionUtils.isEmpty(authors)) {
            return "";
        }

        return getAuthors().stream()
                .map(author -> StringUtils.joinWith(" ", author.getFirstName(), author.getLastName()))
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public String getGenresNames() {
        if (CollectionUtils.isEmpty(genres)) {
            return "";
        }

        return getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    public Double getAverageRating() {
        if (CollectionUtils.isEmpty(reviews)) return 0d;

        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0d);
    }
}
