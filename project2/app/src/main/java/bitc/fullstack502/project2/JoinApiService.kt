package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 데이터 클래스 (회원가입 요청용)
data class JoinRequest(
    val userName: String,
    val userId: String,
    val userPassword: String,
    val userAge: Int,
    val userEmail: String
)

// 회원가입 API 응답 예시
data class JoinResponse(
    val success: Boolean,
    val message: String
)

// 아이디 중복 체크 API 응답 예시
data class IdCheckResponse(
    val available: Boolean,
    val message: String
)

interface JoinApiService {

    // 회원가입 요청 (POST)
    @POST("/api/join")
    fun joinUser(
        @Body joinRequest: JoinRequest
    ): Call<JoinResponse>

    // 아이디 중복 체크 (GET)
    @GET("/api/check-id")
    fun checkIdDuplicate(
        @Query("id") id: String
    ): Call<IdCheckResponse>
}