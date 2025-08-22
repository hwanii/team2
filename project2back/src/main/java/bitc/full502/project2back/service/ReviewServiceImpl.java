package bitc.full502.project2back.service;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;
import bitc.full502.project2back.dto.ReviewUpdateRequestDTO;
import bitc.full502.project2back.entity.ReviewEntity;
import bitc.full502.project2back.entity.UserEntity;
import bitc.full502.project2back.repository.ReviewRepository;
import bitc.full502.project2back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // 스프링 빈으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository; // 사용자 정보를 가져오기 위해 UserRepository 필요

    @Override
    public void createReview(ReviewRequestDTO reviewRequest) {
        // userKey로 사용자를 찾습니다.
        UserEntity user = userRepository.findById((reviewRequest.getUserKey()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + reviewRequest.getUserKey()));

        // 2. DTO와 UserEntity를 기반으로 새로운 ReviewEntity 생성
        ReviewEntity newReview = new ReviewEntity();
        newReview.setUser(user); // JPA 관계에 따라 UserEntity 객체를 설정
        newReview.setPlaceCode(reviewRequest.getPlaceCode());
        newReview.setReviewItem(reviewRequest.getReviewItem());
        // reviewNum, reviewDay 등 필요한 다른 값들도 설정
        newReview.setReviewNum(reviewRequest.getReviewNum());
        // 오늘 날짜를 "yyyy-MM-dd" 형식의 문자열로 설정
        newReview.setReviewDay(LocalDateTime.now());
        // 예: newReview.setReviewNum(...);

        // 3. 데이터베이스에 저장
        reviewRepository.save(newReview);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByPlaceCode(int placeCode) {
        List<ReviewEntity> reviews = reviewRepository.findByPlaceCode(placeCode);

        // Entity 리스트를 DTO 리스트로 변환
        return reviews.stream()
                .map(review -> ReviewResponseDTO.builder()
                        .reviewKey(review.getReviewKey())
                        .userId(review.getUser().getUserId())
                        .userName(review.getUser().getUserName())
                        .reviewRating(review.getReviewNum()) // 필드명 통일
                        .reviewItem(review.getReviewItem())
                        .reviewDay(review.getReviewDay())
                        .userKey(review.getUser().getUserKey())
                        .placeCode(review.getPlaceCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Integer reviewKey) {
        if (!reviewRepository.existsById(reviewKey)) {
            throw new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewKey);
        }
        reviewRepository.deleteById(reviewKey);
    }

    @Override
    public void updateReview(Integer reviewKey, ReviewUpdateRequestDTO reviewUpdateRequest) {
        ReviewEntity reviewEntity = reviewRepository.findById(reviewKey)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewKey));

        // 2. DTO에 담겨온 새로운 정보로 엔티티의 값을 변경합니다.
        reviewEntity.setReviewItem(reviewUpdateRequest.getReviewItem());
        reviewEntity.setReviewNum(reviewUpdateRequest.getReviewNum());
        reviewEntity.setReviewDay(LocalDateTime.now()); // 수정 시각을 현재로 업데이트

        // 3. 변경된 내용을 데이터베이스에 저장(update)합니다.
        reviewRepository.save(reviewEntity);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByUserKey(int userKey) {
        return reviewRepository.findByUser_UserKey(userKey)
                .stream()
                .map(review -> new ReviewResponseDTO(
                        review.getReviewKey(),
                        review.getUser().getUserId(),
                        review.getUser().getUserName(),
                        review.getReviewNum(),
                        review.getReviewItem(),
                        review.getReviewDay(),
                        review.getUser().getUserKey(),
                        review.getPlaceCode()
                ))
                .collect(Collectors.toList());
    }

}