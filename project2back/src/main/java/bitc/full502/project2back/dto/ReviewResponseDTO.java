package bitc.full502.project2back.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponseDTO {
    private Integer reviewKey;
    private String userId;
    private String userName;
    private float reviewRating; // 필드명 통일 (reviewNum -> reviewRating)
    private String reviewItem;
    private String reviewDay;
    private Integer userKey;
}