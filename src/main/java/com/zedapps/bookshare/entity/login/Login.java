package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "logins")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String name;

    @Email(message = "{error.email}")
    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "{error.blank}")
    @Size(max = 1024, message = "{error.max.length.exceeded}")
    private String password;

    @Size(max = 4000, message = "{error.max.length.exceeded}")
    private String bio;

    private String profilePictureUrl;

    @NotNull(message = "{error.required}")
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Book> books;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user")
    private List<Shelf> shelves;

    @OneToMany(mappedBy = "follower")
    private Set<Follow> following;

    @OneToMany(mappedBy = "following")
    private Set<Follow> followers;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
