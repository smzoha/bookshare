package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.enums.AuthProvider;
import com.zedapps.bookshare.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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
@Table(name = "logins")
@NamedEntityGraph(
        name = "login.withCollections",
        attributeNodes = {
                @NamedAttributeNode("profilePicture"),
                @NamedAttributeNode(value = "shelves", subgraph = "shelves-books-subgraph"),
                @NamedAttributeNode(value = "readingProgresses", subgraph = "progress-book-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "shelves-books-subgraph",
                        attributeNodes = @NamedAttributeNode("books")
                ),
                @NamedSubgraph(
                        name = "progress-book-subgraph",
                        attributeNodes = @NamedAttributeNode(value = "book", subgraph = "book-subgraph")
                ),
                @NamedSubgraph(name = "book-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("authors"),
                                @NamedAttributeNode("reviews"),
                                @NamedAttributeNode("image")
                        }
                )
        }
)
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "login_seq")
    @SequenceGenerator(name = "login_seq", sequenceName = "login_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String firstName;

    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String lastName;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String handle;

    @Email(message = "{error.email}")
    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    @Column(unique = true, nullable = false)
    private String email;

    // Password will be empty for SSO users
    @Size(max = 1024, message = "{error.max.length.exceeded}")
    private String password;

    @Size(max = 4000, message = "{error.max.length.exceeded}")
    private String bio;

    @ManyToOne
    @JoinColumn(name = "profile_picture_id")
    private Image profilePicture;

    @NotNull(message = "{error.required}")
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull(message = "{error.required}")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private String providerId;

    private boolean active;

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews = new LinkedHashSet<>();

    @OrderBy("defaultShelf DESC, name")
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<Shelf> shelves = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate, endDate")
    private Set<ReadingProgress> readingProgresses = new LinkedHashSet<>();

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
        if (!(o instanceof Login other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Login{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", handle='" + handle + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public Shelf getShelf(Long shelfId) {
        return getShelves().stream()
                .filter(s -> Objects.equals(s.getId(), shelfId))
                .findFirst()
                .orElseThrow();
    }

    public List<ReadingProgress> getReadingProgresses(Long bookId) {
        return getReadingProgresses().stream()
                .filter(rp -> Objects.equals(rp.getBook().getId(), bookId))
                .toList();
    }

    public ReadingProgress getReadingProgress(Long bookId) {
        return getReadingProgresses().stream()
                .filter(rp -> Objects.equals(rp.getBook().getId(), bookId))
                .findFirst()
                .orElse(null);
    }
}
