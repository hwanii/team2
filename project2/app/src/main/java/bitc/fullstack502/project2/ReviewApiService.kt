package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// 순수한 코틀린 문법으로 작성된 인터페이스
interface ReviewApiService {

    @GET("api/reviews/{placeCode}")
    fun getReviews(@Path("placeCode") placeCode: Int): Call<List<ReviewResponse>>

    @POST("api/reviews")
    fun createReview(
        @Header("Authorization") authToken: String,
        @Body reviewRequest: ReviewRequest
    ): Call<String>
}