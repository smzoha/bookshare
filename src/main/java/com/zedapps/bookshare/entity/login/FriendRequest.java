package com.zedapps.bookshare.entity.login;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * @author smzoha
 * @since 12/9/25
 **/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class FriendRequest extends BaseConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "friend_req_seq")
    @SequenceGenerator(name = "friend_req_seq", sequenceName = "friend_req_seq", allocationSize = 1)
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendRequest other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
