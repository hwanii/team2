package bitc.full502.project2back.repository;

import bitc.full502.project2back.entity.FavoriteEntity;
import bitc.full502.project2back.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Integer> {
  List<FavoriteEntity> findAllByUser(UserEntity user);

  Optional<FavoriteEntity> findByUserAndPlaceCode(UserEntity user, Integer placeCode);

  void deleteByUserAndPlaceCode(UserEntity user, Integer placeCode);

  @Query("SELECT f.placeCode FROM FavoriteEntity f WHERE f.user.userKey = :userKey AND f.isFavorite = true")
  List<Integer> findPlaceCodesByUserKey(@Param("userKey") Integer userKey);
}
