package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinRepository{

    private val joinApiService = RetrofitClient.joinApiService


    // 회원가입 요청 함수
    fun joinUser(
        name: String,
        id: String,
        password: String,
        age: Int,
        email: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val joinRequest = JoinRequest(name, id, password, age, email)
        joinApiService.joinUser(joinRequest).enqueue(object : Callback<JoinResponse> {
            override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onResult(body.success, body.message)
                    } else {
                        onResult(false, "응답 데이터가 없습니다.")
                    }
                } else {
                    onResult(false, "서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                onResult(false, "네트워크 오류: ${t.message}")
            }
        })
    }

    // 아이디 중복 체크 함수
    fun checkIdDuplicate(
        id: String,
        onResult: (available: Boolean, message: String) -> Unit
    ) {
        joinApiService.checkIdDuplicate(id).enqueue(object : Callback<IdCheckResponse> {
            override fun onResponse(call: Call<IdCheckResponse>, response: Response<IdCheckResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        onResult(body.available, body.message)
                    } else {
                        onResult(false, "응답 데이터가 없습니다.")
                    }
                } else {
                    onResult(false, "서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<IdCheckResponse>, t: Throwable) {
                onResult(false, "네트워크 오류: ${t.message}")
            }
        })
    }
}