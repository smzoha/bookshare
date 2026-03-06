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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "friend_req_seq")
    @SequenceGenerator(name = "friend_req_seq", sequenceName = "friend_req_seq", allocationSize = 1)
    private Long id;
}
