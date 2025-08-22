package bitc.full502.project2back.repository; // repository 패키지 안에 생성

import bitc.full502.project2back.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    List<ReviewEntity> findByPlaceCode(int placeCode);
    List<ReviewEntity> findByUser_UserKey(int userKey);
}