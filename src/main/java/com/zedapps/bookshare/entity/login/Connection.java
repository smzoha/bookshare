package com.zedapps.bookshare.entity.login;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Connection extends BaseConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "connection_seq")
    @SequenceGenerator(name = "connection_seq", sequenceName = "connection_seq", allocationSize = 1)
    private Long id;

    public Connection(Login person1, Login person2) {
        setPerson1(person1);
        setPerson2(person2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Connection other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
