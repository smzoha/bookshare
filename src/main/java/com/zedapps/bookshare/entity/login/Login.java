package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.image.Image;
import com.zedapps.bookshare.entity.login.enums.Role;
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
@Table(name = "logins")
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

    @NotBlank(message = "{error.blank}")
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

    private boolean active;

    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Shelf> shelves = new ArrayList<>();

    @OneToMany(mappedBy = "follower")
    private List<Follow> following = new ArrayList<>();

    @OneToMany(mappedBy = "following")
    private List<Follow> followers = new ArrayList<>();

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    public Shelf getShelf(Long shelfId) {
        return getShelves().stream()
                .filter(s -> Objects.equals(s.getId(), shelfId))
                .findFirst()
                .orElseThrow();
    }
}
