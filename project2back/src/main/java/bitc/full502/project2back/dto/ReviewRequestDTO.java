package bitc.full502.project2back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReviewRequestDTO {

    @JsonProperty("review_item")
    private String reviewItem;

    @JsonProperty("place_code")
    private int placeCode;

    @JsonProperty("review_num")
    private float reviewNum;

    @JsonProperty("user_key")
    private int userKey;
}