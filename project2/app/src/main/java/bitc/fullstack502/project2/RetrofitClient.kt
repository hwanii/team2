package bitc.fullstack502.project2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//     기본 시작 url 지정
    private const val BASE_URL = "https://apis.data.go.kr/6260000/"

    val api: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java)
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://your.api.base.url/")  // 실제 API 서버 주소로 바꾸세요
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private const val SPRING_BASE_URL = "http://localhost:8080/api/join"

    val JoinApiService: JoinApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPRING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoinApiService::class.java)
    }
}