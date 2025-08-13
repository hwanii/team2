package bitc.fullstack502.project2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://your.api.base.url/")  // 실제 API 서버 주소로 바꾸세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val joinApiService: JoinApiService by lazy {
        retrofit.create(JoinApiService::class.java)
    }
}