package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shelf_seq")
    @SequenceGenerator(name = "shelf_seq", sequenceName = "shelf_sequence", allocationSize = 1)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login user;

    @OneToMany(mappedBy = "shelf")
    private List<ShelvedBook> books;
}

