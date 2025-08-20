package bitc.full502.project2back.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Data
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_key")
    private Integer reviewKey;

    @ManyToOne
    @JoinColumn(name = "user_key")
    private UserEntity user;

    @Column(name = "review_num", nullable = false)
    private float reviewNum;

    @Column(name = "review_item", nullable = false)
    private String reviewItem;

    @Column(name = "review_day")
    private LocalDateTime reviewDay;

    @Column(name = "place_code")
    private int placeCode;
}
