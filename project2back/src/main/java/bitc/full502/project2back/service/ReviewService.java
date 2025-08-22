package bitc.full502.project2back.service;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;
import bitc.full502.project2back.dto.ReviewUpdateRequestDTO;

import java.util.List;

public interface ReviewService {
    void createReview(ReviewRequestDTO reviewRequest);
    List<ReviewResponseDTO> getReviewsByPlaceCode(int placeCode);
    void deleteReview(Integer reviewKey);
    void updateReview(Integer reviewKey, ReviewUpdateRequestDTO reviewUpdateRequest);
    List<ReviewResponseDTO> getReviewsByUserKey(int userKey);

}
