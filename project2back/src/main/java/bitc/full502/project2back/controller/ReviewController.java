package bitc.full502.project2back.controller;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;
import bitc.full502.project2back.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(
            @RequestBody ReviewRequestDTO reviewRequest,
            Authentication authentication
            ) {
        String userId = authentication.getName();
        reviewService.createReview(userId, reviewRequest);
        return ResponseEntity.ok().body("review created");
    }
    @GetMapping("/reviews/{placeCode}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable("placeCode") int placeCode) {
        List<ReviewResponseDTO> reviewList = reviewService.getReviewsByPlaceCode(placeCode);
        return ResponseEntity.ok(reviewList);
    }
}
