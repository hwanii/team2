package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface JoinApiService {

    @POST("/api/login")
    fun loginUser(@Body request: LoginRequest): Call<UserResponse>

    // 회원가입 요청 (POST)
    @POST("/api/join")
    fun joinUser(@Body joinRequest: JoinRequest): Call<JoinResponse>

    // 아이디 중복 체크 (GET)
    @GET("/api/check-id")
    fun checkIdDuplicate(@Query("id") id: String): Call<IdCheckResponse>

    //  회원 데이터 수정 (POST)
    @POST("/api/edit")
    fun updateUser(@Body request: EditRequest): Call<EditResponse>

    @GET("user/{id}")
    fun getUserById(@Path("id") userId: String): Call<User>

}