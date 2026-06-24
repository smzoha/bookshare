package com.zedapps.bookshare.entity.login;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author smzoha
 * @since 16/6/26
 **/
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReadingChallengeId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long login;
    private int year;
}
