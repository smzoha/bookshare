package com.zedapps.bookshare.entity.book;

import com.zedapps.bookshare.entity.login.Login;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author smzoha
 * @since 28/3/26
 **/
@Data
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
    public String toString() {
        return "AuthorRequest{" +
                "id=" + id +
                ", login=" + login.getEmail() +
                '}';
    }
}
