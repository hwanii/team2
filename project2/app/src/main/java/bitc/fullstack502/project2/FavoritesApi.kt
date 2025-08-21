package bitc.fullstack502.project2

import bitc.fullstack502.project2.model.FavoriteItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApi {
  @GET("favorites/{userKey}")
  fun getFavorites(@Path("userKey") userKey: Int): Call<List<Int>>
  
  @POST("favorites/add")
  fun addFavorite(@Body body: Map<String, Int>): Call<Void>
  
  @POST("favorites/remove")
  fun removeFavorite(@Body body: Map<String, Int>): Call<Void> }