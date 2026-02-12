package com.zedapps.bookshare.entity.activity;

import com.zedapps.bookshare.entity.activity.enums.ActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
public class ActivityOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "{error.required}")
    private ActivityType eventType;

    @Size(max = 255, message = "{error.max.length.exceeded}")
    @NotNull(message = "{error.required}")
    private String referenceEntity;

    private Long referenceId;

    @NotNull(message = "{error.required}")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @Size(max = 255)
    @NotNull(message = "{error.required}")
    @ColumnDefault("'PENDING'")
    private String status;

    private int retryCount;

    @PastOrPresent
    @CreationTimestamp
    @NotNull(message = "{error.required}")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @NotNull(message = "{error.required}")
    private LocalDateTime processedAt;
}