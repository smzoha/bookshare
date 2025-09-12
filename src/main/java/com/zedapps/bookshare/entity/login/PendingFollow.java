package com.zedapps.bookshare.entity.login;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PendingFollow extends BaseFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "follow_seq")
    @SequenceGenerator(name = "follow_seq", sequenceName = "follow_seq", allocationSize = 1)
    private Long id;
}
