package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// 순수한 코틀린 문법으로 작성된 인터페이스

data class ReviewUpdateRequest (
    val reviewItem: String,
    val reviewNum : Float
)
interface ReviewApiService {

    @GET("api/reviews/{placeCode}")
    fun getReviews(@Path("placeCode") placeCode: Int): Call<List<ReviewResponse>>

    @POST("api/reviews")
    fun createReview(
        @Body reviewRequest: ReviewRequest
    ): Call<String>

    @POST("/reviews/add")
    fun submitReview(@Body reviewData: ReviewRequest): Call<Void>

    @DELETE("api/reviews/{reviewKey}")
    fun deleteReview(@Path("reviewKey") reviewKey: Int): Call<String>

    @PUT("api/reviews/{reviewKey}")
    fun updateReview(
        @Path("reviewKey") reviewKey: Int,
        @Body reviewUpdateRequest: ReviewUpdateRequest
    ): Call<String>

}