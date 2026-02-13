package com.zedapps.bookshare.entity.activity;

import com.zedapps.bookshare.entity.activity.enums.ActivityStatus;
import com.zedapps.bookshare.entity.activity.enums.ActivityType;
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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @NotNull(message = "{error.required}")
    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    private int retryCount;

    @PastOrPresent
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime processedAt;
}