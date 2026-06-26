package com.zedapps.bookshare.entity.login;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @author smzoha
 * @since 16/6/26
 **/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ReadingChallengeId.class)
@EqualsAndHashCode
public class ReadingChallenge {

    @Id
    @ManyToOne
    private Login login;

    @Id
    private int year;

    @NotNull(message = "{error.required}")
    @Min(value = 1, message = "{error.min.value}")
    @Max(value = 1000, message = "{error.max.value}")
    private Integer bookCount;
}
