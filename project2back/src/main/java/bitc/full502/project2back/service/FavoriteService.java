package bitc.full502.project2back.service;

import bitc.full502.project2back.entity.FavoriteEntity;
import bitc.full502.project2back.entity.UserEntity;
import bitc.full502.project2back.repository.FavoriteRepository;
import bitc.full502.project2back.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
  private final FavoriteRepository favoriteRepository;
  private final UserService userService;

  public FavoriteService(FavoriteRepository favoriteRepository, UserService userService) {
    this.favoriteRepository = favoriteRepository;
    this.userService = userService;
  }

  // 즐겨찾기 목록 가져오기
  public List<Integer> getFavorites(Integer userKey) {
    UserEntity user = userService.findByUserKey(userKey);
    return favoriteRepository.findAllByUser(user)
        .stream()
        .map(FavoriteEntity::getPlaceCode)
        .collect(Collectors.toList());
  }

  // 즐겨찾기 추가
  public void addFavorite(Integer userKey, Integer placeCode) {
    UserEntity user = userService.findByUserKey(userKey);
    FavoriteEntity favorite = favoriteRepository.findByUserAndPlaceCode(user, placeCode)
        .orElse(FavoriteEntity.builder()
            .user(user)
            .placeCode(placeCode)
            .isFavorite(true)
            .build());
    favorite.setFavorite(true);
    favoriteRepository.save(favorite);
  }

  // 즐겨찾기 해제
  @Transactional
  public void removeFavorite(Integer userKey, Integer placeCode) {
    UserEntity user = userService.findByUserKey(userKey);
    favoriteRepository.deleteByUserAndPlaceCode(user, placeCode);
  }
}
