package bitc.fullstack502.project2

import bitc.fullstack502.project2.model.FavoriteItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoritesApi {
  @GET("favorites")
  fun getFavorites(@Query("userKey") userKey: Int): Call<List<FavoriteItem>>
  
  @POST("favorites")
  @FormUrlEncoded
  fun addFavorite(
    @Field("userKey") userKey: Int,
    @Field("placeCode") placeCode: Int
  ): Call<Void>
  
  @HTTP(method = "DELETE", path = "favorites", hasBody = true)
  fun removeFavorite(@Body body: Map<String, Int>): Call<Void>
}