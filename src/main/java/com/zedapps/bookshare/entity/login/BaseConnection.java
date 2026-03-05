package com.zedapps.bookshare.entity.login;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * The idea is to create an instance of FriendRequest first, and then copy it over to the Connection table.
 * As such, FriendRequest will make use of the sequence, whereas the data will be copied over to Connection
 * once users accept the request.
 *
 * @author smzoha
 * @since 12/9/25
 **/
@MappedSuperclass
@Getter
@Setter
public class BaseConnection {

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "person1_id")
    private Login person1;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "person2_id")
    private Login person2;
}
