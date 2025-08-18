package bitc.fullstack502.project2

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitClient {
    private const val FOOD_BASE_URL = "https://apis.data.go.kr/6260000/"

    val api: FoodApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder().setLenient().create()

        Retrofit.Builder()
            .baseUrl(FOOD_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FoodApiService::class.java)
    }
}
