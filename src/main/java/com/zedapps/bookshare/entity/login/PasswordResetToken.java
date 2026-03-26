package com.zedapps.bookshare.entity.login;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 26/3/26
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "password_reset_token_seq")
    @SequenceGenerator(name = "password_reset_token_seq", sequenceName = "password_reset_token_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 1000)
    private String hashedSignature;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private LocalDateTime generatedAt;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime expiryTimestamp;
}
