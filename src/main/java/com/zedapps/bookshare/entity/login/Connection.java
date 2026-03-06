package com.zedapps.bookshare.entity.login;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Connection extends BaseConnection {

    @Id
    private Long id;

    public Connection(Login person1, Login person2) {
        setPerson1(person1);
        setPerson2(person2);
    }
}

