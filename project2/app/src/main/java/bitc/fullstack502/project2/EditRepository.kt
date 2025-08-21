package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRepository(private val apiService: JoinApiService) {

    fun updateUser(
        name: String,
        id: String,
        pw: String,
        tel: String,
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val request = EditRequest(
            userName = name,
            userId = id,
            userPw = pw,
            userTel = tel,
            userEmail = email
        )
        apiService.updateUser(request).enqueue(object : Callback<EditResponse> {
            override fun onResponse(call: Call<EditResponse>, response: Response<EditResponse>) {
                val body = response.body()
                onResult(body?.success ?: false, body?.message ?: "응답 데이터 없음")
            }

            override fun onFailure(call: Call<EditResponse>, t: Throwable) {
                onResult(false, "네트워크 오류: ${t.message}")
            }
        })
    }

    //  사용자 정보 가져오기
    fun getUserById(userId: String, onResult: (User) -> Unit) {
        apiService.getUserById(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if (user != null) {
                    onResult(user)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // 필요시 에러 핸들링 추가
            }
        })
    }
}
