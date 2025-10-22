package com.zedapps.bookshare.entity.login;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @SequenceGenerator(name = "shelf_seq", sequenceName = "shelf_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{error.blank}")
    @Size(max = 255, message = "{error.max.length.exceeded}")
    private String name;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login user;

    @OneToMany(mappedBy = "shelf")
    private List<ShelvedBook> books = new ArrayList<>();
}

