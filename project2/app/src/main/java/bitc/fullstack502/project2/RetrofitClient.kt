package bitc.fullstack502.project2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 기본 시작 url 지정 (공공데이터 API)
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
            .baseUrl("https://your.api.base.url/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private const val SPRING_BASE_URL = "http://10.0.2.2:8080/"

    // 회원가입 & 로그인 API
    val joinApi: JoinApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPRING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoinApiService::class.java)
    }

    // 회원정보 수정 API
    val editApi: JoinApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPRING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JoinApiService::class.java)
    }
}
