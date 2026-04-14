package com.zedapps.bookshare.entity.book;

import com.zedapps.bookshare.entity.login.Login;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author smzoha
 * @since 28/3/26
 **/
@Getter
@Setter
@Entity
@Table(name = "author_request")
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_request_seq")
    @SequenceGenerator(name = "author_request_seq", sequenceName = "author_request_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    private Login login;

    public AuthorRequest(Login login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthorRequest other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AuthorRequest{" +
                "id=" + id +
                ", login=" + login.getEmail() +
                '}';
    }
}
