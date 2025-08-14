package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository {

    private val apiService = RetrofitClient.joinApi

    fun loginUser(
        id: String,
        pw: String,
        onResult: (success: Boolean, user: UserResponse?, message: String) -> Unit
    ) {
        apiService.loginUser(LoginRequest(id, pw)).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onResult(true, body, "로그인 성공")
                    } else {
                        onResult(false, null, "서버 응답 데이터 없음")
                    }
                } else {
                    onResult(false, null, "서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                onResult(false, null, "네트워크 오류: ${t.message}")
            }
        })
    }
}
