package bitc.fullstack502.project2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.FavoritesAdapter
import bitc.fullstack502.project2.databinding.ActivityFavoritesBinding
import bitc.fullstack502.project2.model.FavoriteItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavoritesAdapter
    private val currentUserKey = 1 // 로그인 시 받아오는 userKey
    private val likedPlaceCodes = mutableSetOf<Int>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        
        loadFavorites()
    }
    
    private fun loadFavorites() {
        // 1. DB에서 로그인 유저 즐겨찾기 가져오기
        RetrofitClient.favoritesApi.getFavorites(currentUserKey)
            .enqueue(object : Callback<List<FavoriteItem>> {
                override fun onResponse(
                    call: Call<List<FavoriteItem>>,
                    response: Response<List<FavoriteItem>>
                ) {
                    if (response.isSuccessful) {
                        val favoriteList = response.body() ?: emptyList()
                        likedPlaceCodes.clear()
                        likedPlaceCodes.addAll(favoriteList.map { it.placeCode })
                        
                        // 2. 인텐트로 받은 전체 FoodItem 리스트
                        val allItems: List<FoodItem> =
                            intent.getParcelableArrayListExtra("full_list") ?: emptyList()
                        val favoriteFoodItems = allItems.filter { it.UcSeq in likedPlaceCodes }
                        
                        // 3. 어댑터 연결
                        adapter = FavoritesAdapter(favoriteFoodItems.toMutableList(), favoriteFoodItems, likedPlaceCodes)
                        binding.favoritesRecyclerView.adapter = adapter
                        
                        // 4. 하트 클릭 토글 처리
                        adapter.setOnLikeClickListener(object : FavoritesAdapter.OnLikeClickListener {
                            override fun onLikeToggle(item: FoodItem, position: Int, isLiked: Boolean) {
                                if (isLiked) {
                                    // 즐겨찾기 추가
                                    RetrofitClient.favoritesApi.addFavorite(currentUserKey, item.UcSeq)
                                        .enqueue(object : Callback<Void> {
                                            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                Log.e("FavoritesActivity", "즐찾 추가 오류", t)
                                            }
                                        })
                                } else {
                                    // 즐겨찾기 해제
                                    val body = mapOf("userKey" to currentUserKey, "placeCode" to item.UcSeq)
                                    RetrofitClient.favoritesApi.removeFavorite(body)
                                        .enqueue(object : Callback<Void> {
                                            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                Log.e("FavoritesActivity", "즐찾 해제 오류", t)
                                            }
                                        })
                                }
                            }
                        })
                    } else {
                        Log.e("FavoritesActivity", "즐겨찾기 로딩 실패")
                    }
                }
                
                override fun onFailure(call: Call<List<FavoriteItem>>, t: Throwable) {
                    Log.e("FavoritesActivity", "네트워크 오류", t)
                }
            })
    }
}