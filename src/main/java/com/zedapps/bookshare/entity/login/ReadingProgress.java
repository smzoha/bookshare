package com.zedapps.bookshare.entity.login;

import com.zedapps.bookshare.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * @author smzoha
 * @since 13/1/26
 **/
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reading_prog_seq")
    @SequenceGenerator(name = "reading_prog_seq", sequenceName = "reading_prog_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Login user;

    @NotNull(message = "{error.required}")
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Min(value = 0, message = "{error.min.value}")
    private Long pagesRead = 0L;

    @NotNull(message = "{error.required}")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime endDate;

    private boolean completed;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "ReadingProgress{" +
                "id=" + id +
                ", user=" + user +
                ", pagesRead=" + pagesRead +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", completed=" + completed +
                ", updatedAt=" + updatedAt +
                ", book=" + book.getId() +
                '}';
    }

    public Double getPercentile() {
        double percentile = ((double) getPagesRead() / getBook().getPages()) * 100;

        BigDecimal percentileDecimal = BigDecimal.valueOf(percentile);
        percentileDecimal = percentileDecimal.setScale(2, RoundingMode.HALF_UP);

        return percentileDecimal.doubleValue();
    }
}
