package bitc.full502.project2back.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "favorite",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_key", "place_code"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FavoriteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "favorite_key")
  private Integer favoriteKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_key", nullable = false)
  private UserEntity user;

  @Column(name = "place_code", nullable = false)
  private int placeCode;

  @Column(name = "is_favorite", nullable = false)
  private boolean isFavorite = true;

  @Column(name = "fav_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime favDate = LocalDateTime.now();

  @Column(name = "update_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private LocalDateTime updateDate = LocalDateTime.now();

  // 필요하면 Lombok으로 커스텀 생성자도 추가 가능
  public FavoriteEntity(UserEntity user, int placeCode, boolean isFavorite) {
    this.user = user;
    this.placeCode = placeCode;
    this.isFavorite = isFavorite;
  }
}
