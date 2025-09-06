package com.zedapps.bookshare.entity.book;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;


/**
 * @author smzoha
 * @since 6/9/25
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    @SequenceGenerator(name = "tag_seq", sequenceName = "tag_sequence", allocationSize = 1)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Book> books;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

