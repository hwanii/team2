package bitc.fullstack502.project2

import android.content.Intent
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
    protected fun setupBottomNavigation(bottomNavView: BottomNavigationView) {
        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.menu_list -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.menu_favorite -> {
                    if (!foodList.isNullOrEmpty()) {
                        val mockFavorites = foodList.shuffled().take(6)
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "favorites_list",
                                ArrayList(mockFavorites)
                            )
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "아직 데이터 로딩 중입니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    true
                }
                R.id.menu_profile -> {
                    val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                    val intent =
                        if (isLoggedIn) Intent(this, MyPageActivity::class.java)
                        else Intent(this, LoginPageActivity::class.java).also {
                            Toast.makeText(this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    // ===================== 데이터 가져오기 =====================
    protected fun fetchFoodData(
        onSuccess: (List<FoodItem>) -> Unit,
        onError: () -> Unit
    ) {
        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                if (response.isSuccessful) {
                    val rawList = response.body()?.getFoodkr?.item ?: emptyList()
                    foodList = rawList.filter { !it.thumb.isNullOrBlank() }
                        .distinctBy { it.Lat to it.Lng }
                    onSuccess(foodList)
                } else {
                    onError()
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                onError()
            }
        })
    }
}
