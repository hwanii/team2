package bitc.full502.project2back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ReviewResponseDTO {
    private Integer reviewKey;
    private String userId;
    private String userName;
    private float reviewRating; // 필드명 통일 (reviewNum -> reviewRating)
    private String reviewItem;
    private LocalDateTime reviewDay;
    private Integer userKey;
    private Integer placeCode;
}