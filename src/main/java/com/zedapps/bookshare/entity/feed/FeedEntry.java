package com.zedapps.bookshare.entity.feed;

import com.zedapps.bookshare.entity.activity.Activity;
import com.zedapps.bookshare.entity.login.Login;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
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

    @NotNull
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeedEntry(Login audienceLogin, Activity activity) {
        this.audienceLogin = audienceLogin;
        this.activity = activity;
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