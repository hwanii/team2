package bitc.fullstack502.project2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.FavoritesAdapter
import bitc.fullstack502.project2.databinding.ActivityFavoritesBinding

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. MainActivity에서 전체 음식 리스트를 받아옴

        val allItems: List<FoodItem> = intent.getParcelableArrayListExtra("full_list") ?: emptyList()

        // 2. SharedPreferences에서 저장된 '좋아요' ID 목록을 불러옴
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val likedItemIds = prefs.getStringSet("liked_items", emptySet()) ?: emptySet()

        // 3. 전체 리스트에서 '좋아요'한 아이템들만 필터링 (수정 가능한 MutableList로)
        val favoriteItems: List<FoodItem> = intent.getParcelableArrayListExtra("favorites_list") ?: emptyList()

        Log.d("DEBUG_FAVORITES", "전달받은 즐겨찾기 개수: ${favoriteItems.size}개")
        // 4. 어댑터 설정
        adapter = FavoritesAdapter(favoriteItems.toMutableList(), favoriteItems)
        binding.favoritesRecyclerView.adapter = adapter
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)

        // 5. 어댑터에 하트 아이콘 클릭 리스너 설정 (즐겨찾기 해제 기능)
        adapter.setOnLikeClickListener(object : FavoritesAdapter.OnLikeClickListener {
            override fun onUnlikeClick(item: FoodItem, position: Int) {
                // SharedPreferences에서 ID 제거
                val editor = prefs.edit()
                val currentLikedItems = prefs.getStringSet("liked_items", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                currentLikedItems.remove(item.UcSeq.toString())
                editor.putStringSet("liked_items", currentLikedItems)
                editor.apply()

                // 어댑터에서 아이템 제거 및 화면 갱신
                adapter.removeItem(position)
            }
        })
    }
}