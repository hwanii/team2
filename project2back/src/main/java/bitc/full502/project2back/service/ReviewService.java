package bitc.full502.project2back.service;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {
    void createReview(String userId, ReviewRequestDTO reviewRequest);
    List<ReviewResponseDTO> getReviewsByPlaceCode(int placeCode);
}
