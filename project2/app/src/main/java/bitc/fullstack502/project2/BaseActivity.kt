package bitc.fullstack502.project2

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseActivity : AppCompatActivity() {

    protected var foodList: List<FoodItem> = emptyList()
    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    // ===================== 바텀 네비 =====================
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
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
                    if (foodList.isNotEmpty()) {
                        val mockFavorites = foodList.shuffled().take(6)
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "full_list",
                                ArrayList(foodList) // 전체 음식 리스트 전달
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

    // ===================== 데이터 가져오기 =====================
    private fun fetchFoodData() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val body = response.body()
                    val rawList = body?.getFoodkr?.item ?: emptyList()

                    // 이미지 없는 항목 제거 & 위도/경도로 중복 제거
                    foodList = rawList.filter { !it.thumb.isNullOrBlank() }
                        .distinctBy { it.Lat to it.Lng }

                    // HorizontalAdapter 초기화
                    setupHorizontalAdapters()

                    // VerticalAdapter에 전체 데이터 세팅
                    filterByGu("전체")
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "데이터 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    protected fun setupSearchButton(searchButton: View) {
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }
}
