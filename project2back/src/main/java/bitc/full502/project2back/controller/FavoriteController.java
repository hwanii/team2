package bitc.full502.project2back.controller;

import bitc.full502.project2back.repository.FavoriteRepository;
import bitc.full502.project2back.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*") // 안드로이드에서 접근 가능하도록
public class FavoriteController {

  private final FavoriteService favoriteService;
  private final FavoriteRepository favoriteRepository;

  public FavoriteController(FavoriteService favoriteService, FavoriteRepository favoriteRepository) {
    this.favoriteService = favoriteService;
    this.favoriteRepository = favoriteRepository;
  }

  @GetMapping("/{userKey}")
  public List<Integer> getFavorites(@PathVariable int userKey) {
    return favoriteService.getFavorites(userKey);
  }

  @GetMapping("/debug/{userKey}")
  public ResponseEntity<?> debugFavorites(@PathVariable int userKey) {
    List<Integer> placeCodes = favoriteRepository.findPlaceCodesByUserKey(userKey);
    System.out.println("UserKey: " + userKey + " -> " + placeCodes);
    return ResponseEntity.ok(placeCodes);
  }

  @PostMapping("/add")
  public void addFavorite(@RequestBody Map<String, Integer> body) {
    Integer userKey = body.get("userKey");
    Integer placeCode = body.get("placeCode");
    favoriteService.addFavorite(userKey, placeCode);
  }

  @PostMapping("/remove")
  public void removeFavorite(@RequestBody Map<String, Integer> body) {
    Integer userKey = body.get("userKey");
    Integer placeCode = body.get("placeCode");
    favoriteService.removeFavorite(userKey, placeCode);
  }
}