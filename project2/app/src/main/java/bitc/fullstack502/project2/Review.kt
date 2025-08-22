package bitc.fullstack502.project2 // 본인 프로젝트 패키지

import com.google.gson.annotations.SerializedName

// 서버로 리뷰를 보낼 때 사용할 데이터 모델 (ReviewRequestDTO 매칭)
data class ReviewRequest(
    @SerializedName("user_key") val userKey: Int,
    @SerializedName("review_item") val reviewItem: String,
    @SerializedName("place_code") val placeCode: Int,
    @SerializedName("review_num") val reviewNum: Float
)

// 서버에서 리뷰 목록을 받아올 때 사용할 데이터 모델 (ReviewResponseDTO 매칭)
data class ReviewResponse(
    @SerializedName("reviewKey") val reviewKey: Int,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("reviewRating") val reviewRating: Float,
    @SerializedName("reviewItem") val reviewItem: String,
    @SerializedName("reviewDay") val reviewDay: String,
    @SerializedName("userKey") val userKey: Int,
    @SerializedName("placeCode") val placeCode: Int
)