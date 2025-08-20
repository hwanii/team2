package bitc.full502.project2back.dto;

import lombok.Data;

@Data
public class ReviewUpdateRequestDTO {
    private String reviewItem;
    private float reviewNum; // 안드로이드의 ReviewRequest와 필드명을 통일합니다.
}