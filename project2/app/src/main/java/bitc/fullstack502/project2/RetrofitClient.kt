package bitc.fullstack502.project2

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    
    // 공공데이터 API
    private const val BASE_URL = "https://apis.data.go.kr/6260000/"
    val api: FoodApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        
        val gson = GsonBuilder().setLenient().create()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
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
    
    val favoritesApi: FavoritesApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        
        val gson = GsonBuilder().setLenient().create()
        
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")  // 스프링부트 서버 주소
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FavoritesApi::class.java)
    }

    val reviewApi: ReviewApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SPRING_BASE_URL) // 스프링 서버 주소 사용
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReviewApiService::class.java)
    }
}
