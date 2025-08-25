package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.FavoritesAdapter
import bitc.fullstack502.project2.databinding.ActivityFavoritesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavoritesAdapter
    private val likedPlaceCodes = mutableSetOf<Int>()
    private var currentUserKey: Int = 0
    private var fullFoodList: List<FoodItem> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        // 1️⃣ Intent에서 user_key와 전체 리스트 받아오기
        currentUserKey = intent.getIntExtra("user_key", 0)
        val allItems: List<FoodItem> = intent.getParcelableArrayListExtra("full_list") ?: emptyList()
        
        // 2️⃣ RecyclerView 세팅
        adapter = FavoritesAdapter(mutableListOf(), likedPlaceCodes, currentUserKey)
        binding.favoritesRecyclerView.adapter = adapter
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // 3️⃣ 서버에서 즐겨찾기 가져오기
        RetrofitClient.favoritesApi.getFavorites(currentUserKey)
            .enqueue(object : Callback<List<Int>> {
                override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                    if (response.isSuccessful) {
                        val favoritePlaceCodes = response.body() ?: emptyList()
                        
                        // 즐겨찾기 placeCode 리스트 갱신
                        likedPlaceCodes.clear()
                        likedPlaceCodes.addAll(favoritePlaceCodes)
                        
                        // 전체 리스트에 isBookmarked 반영
                        allItems.forEach { it.isBookmarked = it.UcSeq in likedPlaceCodes && it.UcSeq != 0 }
                        
                        // 즐겨찾기 아이템만 뽑아서 어댑터에 전달
                        val favoriteFoodItems = allItems.filter { it.isBookmarked }
                        adapter.updateItems(favoriteFoodItems.toMutableList())
                        
                        binding.emptyTextView.visibility =
                            if (favoriteFoodItems.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Log.e("FavoritesActivity", "즐겨찾기 로딩 실패: ${response.code()}")
                    }
                }
                
                override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                    Log.e("FavoritesActivity", "네트워크 오류", t)
                }
            })
        
        // 4️⃣ 하트 클릭 이벤트 처리
        adapter.setOnLikeClickListener { item, position, isLiked ->
            val body = mapOf("userKey" to currentUserKey, "placeCode" to item.UcSeq)
            val call = if (isLiked) {
                RetrofitClient.favoritesApi.addFavorite(body)
            } else {
                RetrofitClient.favoritesApi.removeFavorite(body)
            }
            
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        if (isLiked) {
                            likedPlaceCodes.add(item.UcSeq)
                            adapter.notifyItemChanged(position)
                            
                            if (adapter.itemCount == 0) {
                                binding.emptyTextView.visibility = View.VISIBLE
                            }
                        } else {
                            likedPlaceCodes.remove(item.UcSeq)
                            
                            // ✅ 즐겨찾기 목록에서도 제거
                            adapter.removeAt(position)
                            
                            if (adapter.itemCount == 0) {
                                binding.emptyTextView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        Toast.makeText(this@FavoritesActivity, "즐겨찾기 업데이트 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@FavoritesActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
        }
        
        adapter.setOnItemClickListener { item ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("clicked_item", item)
                putParcelableArrayListExtra(
                    "full_list",
                    ArrayList(adapter.getItems())
                )
                putExtra("user_key", currentUserKey)
            }
            startActivity(intent)
        }
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    // 현재 Activity 스택을 초기화하고 MainActivity를 새로 시작하고 싶으면 아래 플래그 추가 가능
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.menu_list -> {
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val currentUserKey = prefs.getInt("user_key", 0)
                    Log.d("ListActivity", "userKey: $currentUserKey")
                    
                    val intent = Intent(this, ListActivity::class.java)
                    intent.putExtra("user_key", currentUserKey)
                    startActivity(intent)
                    true
                    
                }
                R.id.menu_favorite -> {
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val currentUserKey = prefs.getInt("user_key", 0)
                    Log.d("FavoritesActivity", "userKey: $currentUserKey")
                    if (!fullFoodList.isNullOrEmpty()) {
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "full_list",
                                ArrayList(fullFoodList) // 전체 음식 리스트 전달
                            )
                            putExtra("user_key", currentUserKey) // 로그인한 유저 키 전달
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "아직 데이터 로딩 중입니다.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                
                R.id.menu_profile -> {
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                    val intent =
                        if (isLoggedIn) Intent(this, MyPageActivity::class.java)
                        else Intent(this, LoginPageActivity::class.java).also {
                            Toast.makeText(
                                this,
                                "로그인 후 이용 가능합니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
