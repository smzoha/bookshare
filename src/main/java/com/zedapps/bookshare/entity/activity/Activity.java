package com.zedapps.bookshare.entity.activity;

import com.zedapps.bookshare.entity.login.Login;
import com.zedapps.bookshare.enums.ActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(generator = "activity_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "activity_seq", sequenceName = "activity_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{error.required}")
    private ActivityType eventType;

    @NotNull(message = "{error.required}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String referenceEntity;

    private Long referenceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @PastOrPresent
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    private boolean internal;

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", login=" + login.getEmail() +
                ", eventType=" + eventType +
                ", referenceEntity='" + referenceEntity + '\'' +
                ", referenceId=" + referenceId +
                ", metadata=" + metadata +
                ", createdAt=" + createdAt +
                ", internal=" + internal +
                '}';
    }
}