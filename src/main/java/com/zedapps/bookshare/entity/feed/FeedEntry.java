package com.zedapps.bookshare.entity.feed;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.login.Login;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "feed_entry")
@NoArgsConstructor
@AllArgsConstructor
public class FeedEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feed_entry_seq")
    @SequenceGenerator(name = "feed_entry_seq", sequenceName = "feed_entry_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "audience_login", nullable = false)
    private Login audienceLogin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeedEntry(Login audienceLogin, Activity activity) {
        this.audienceLogin = audienceLogin;
        this.activity = activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedEntry other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FeedEntry{" +
                "id=" + id +
                ", audienceLogin=" + audienceLogin.getEmail() +
                ", activity=" + activity.toString() +
                ", createdAt=" + createdAt +
                '}';
    }
}
