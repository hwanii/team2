package bitc.full502.project2back.service;

import bitc.full502.project2back.dto.ReviewRequestDTO;
import bitc.full502.project2back.dto.ReviewResponseDTO;
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
    public void createReview(String userId, ReviewRequestDTO reviewRequest) {
        // 1. userId를 이용해 UserEntity 찾기
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

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
                        .reviewDay(review.getReviewDay().toString())
                        .build())
                .collect(Collectors.toList());
    }
}