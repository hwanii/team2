package bitc.fullstack502.project2

import bitc.fullstack502.project2.FoodResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {
    @GET("FoodService/getFoodKr")
    fun getFoodList(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("resultType") resultType: String = "json",
        @Query("numOfRows") numOfRows: Int = 5000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("TITLE") title: String? = null,
        @Query("GUGUN_NM") gugun: String? = null,
        @Query("uc_seq") ucSeq: Int? = null
    ): Call<FoodResponse>

}
