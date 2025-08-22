package bitc.full502.project2back.controller;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;
import bitc.full502.project2back.dto.ReviewUpdateRequestDTO;
import bitc.full502.project2back.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(@RequestBody ReviewRequestDTO reviewRequest) {
        try {
            // 안드로이드에서 받은 reviewRequest를 서비스로 전달해 리뷰를 저장합니다.
            reviewService.createReview(reviewRequest);
            return ResponseEntity.ok().body("리뷰가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            // userKey가 잘못되었거나 다른 문제가 발생했을 때
            return ResponseEntity.badRequest().body("리뷰 등록 실패: " + e.getMessage());
        }
    }
    @GetMapping("/reviews/{placeCode}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable("placeCode") int placeCode) {
        List<ReviewResponseDTO> reviewList = reviewService.getReviewsByPlaceCode(placeCode);
        return ResponseEntity.ok(reviewList);

    }
    @DeleteMapping("/reviews/{reviewKey}")
    public ResponseEntity<String> deleteReview(@PathVariable("reviewKey") Integer reviewKey) {
        try {
            reviewService.deleteReview(reviewKey);
            return ResponseEntity.ok().body("리뷰가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("리뷰 삭제 실패: " + e.getMessage());
        }
    }

    @PutMapping("/reviews/{reviewKey}")
    public ResponseEntity<String> updataReview(@PathVariable("reviewKey") Integer reviewKey, @RequestBody ReviewUpdateRequestDTO reviewUpdateRequest) {
    try {
        reviewService.updateReview(reviewKey, reviewUpdateRequest);
        return ResponseEntity.ok().body("리뷰 수정완료");
    }catch (Exception e) {
        return ResponseEntity.badRequest().body("리뷰 수정 실패: " + e.getMessage());
    }
    }

    @GetMapping("/reviews/user/{userKey}")
    public ResponseEntity<List<ReviewResponseDTO>> getUserReviews(@PathVariable("userKey") int userKey) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByUserKey(userKey);
        return ResponseEntity.ok(reviews);
    }

}
