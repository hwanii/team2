package bitc.fullstack502.project2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {
    @GET("FoodService/getFoodKr")
    fun getFoodList(
//        encoded = true = 암호화된 키 값을 한번더 암호화 하지 못하게 막는 것. 지우면 작동안됨
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("resultType") resultType: String = "json",
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,   // 페이지 리스트 기능 만드실때 이 부분을 변수로 만드시면 됩니다 ( = 1 ) 삭제
        @Query("TITLE") title: String? = null,
        @Query("GUGUN_NM") gugun: String? = null
    ) : Call<FoodResponse>
}