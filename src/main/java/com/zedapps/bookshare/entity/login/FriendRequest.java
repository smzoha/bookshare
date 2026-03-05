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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FriendRequest extends BaseConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "connection_seq")
    @SequenceGenerator(name = "connection_seq", sequenceName = "connection_seq", allocationSize = 1)
    private Long id;
}
