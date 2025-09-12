package com.zedapps.bookshare.entity.login;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;

/**
 * The idea is to create an instance of PendingFollow first, and then copy it over to the Follow table.
 * As such, PendingFollow will make use of the sequence, whereas the data will be copied over to Follow
 * once users accept the request.
 *
 * @author smzoha
 * @since 12/9/25
 **/
@MappedSuperclass
public class BaseFollow {

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "follower_id")
    private Login follower;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "following_id")
    private Login following;
}
