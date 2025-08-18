package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
// import bitc.fullstack502.project2.RetrofitClient  // 삭제해도 됨

class JoinRepository {

    // joinApiService는 현재 사용 안하므로 주석 처리
    // private val joinApiService = RetrofitClient.joinApiService

    fun joinUser(
        name: String,
        id: String,
        password: String,
        age: Int,
        email: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        // joinApiService 사용 코드 주석 처리
        /*
        val joinRequest = JoinRequest(name, id, password, age, email)
        joinApiService.joinUser(joinRequest).enqueue(object : Callback<JoinResponse> {
            override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                ...
            }
            override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                ...
            }
        })
        */
    }

    fun checkIdDuplicate(
        id: String,
        onResult: (available: Boolean, message: String) -> Unit
    ) {
        // joinApiService 사용 코드 주석 처리
        /*
        joinApiService.checkIdDuplicate(id).enqueue(object : Callback<IdCheckResponse> {
            override fun onResponse(call: Call<IdCheckResponse>, response: Response<IdCheckResponse>) {
                ...
            }
            override fun onFailure(call: Call<IdCheckResponse>, t: Throwable) {
                ...
            }
        })
        */
    }
}
