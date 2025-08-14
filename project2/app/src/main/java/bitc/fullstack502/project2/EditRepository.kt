package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRepository(private val apiService: JoinApiService) {

    fun updateUser(
        name: String,
        id: String,
        password: String,
        tel: String,
        email: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val request = EditRequest(name, id, password, tel, email)
        apiService.updateUser(request).enqueue(object : Callback<EditResponse> {
            override fun onResponse(call: Call<EditResponse>, response: Response<EditResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    onResult(body?.success ?: false, body?.message ?: "응답 없음")
                } else {
                    onResult(false, "서버 오류: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<EditResponse>, t: Throwable) {
                onResult(false, "네트워크 오류: ${t.message}")
            }
        })
    }
}
