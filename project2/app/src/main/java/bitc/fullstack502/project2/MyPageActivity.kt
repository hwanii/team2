package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMyPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageActivity : AppCompatActivity() {
    private var foodList: List<FoodItem>? = null
    private lateinit var binding: ActivityMyPageBinding

    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 유저 정보 불러오기
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val user = User(
            userName = prefs.getString("user_name", "") ?: "",
            userId = prefs.getString("user_id", "") ?: "",
            userPw = prefs.getString("user_pw", "") ?: "",
            userTel = prefs.getString("user_tel", "") ?: "",
            userEmail = prefs.getString("user_email", "") ?: ""
        )

        if (user.userName.isNotEmpty() && user.userId.isNotEmpty()) {
            binding.userName.text = "이름 : ${user.userName}"
            binding.userId.text = "ID : ${user.userId}"
        } else {
            Log.d("MyPageActivity", "User info not found in SharedPreferences")
        }

        // 🔹 프로필 수정 버튼
        binding.goEdit.setOnClickListener {
            val intent = Intent(this, EditPageActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        // 🔹 로그아웃 버튼
        binding.logoutButton.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }

        // 🔹 리뷰 유무 확인
        val hasReview = false
        if (hasReview) {
            binding.myReview.visibility = View.VISIBLE
            binding.noReview.visibility = View.GONE
        } else {
            binding.myReview.visibility = View.GONE
            binding.noReview.visibility = View.VISIBLE
        }

        // 🔹 Retrofit으로 데이터 불러오기
        fetchFoodData()

        // 🔹 하단 네비게이션
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
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
                        val mockFavorites = foodList!!.shuffled().take(6)
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra("favorites_list", ArrayList(mockFavorites))
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "아직 데이터 로딩 중입니다.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_profile -> {
                    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                    val intent =
                        if (isLoggedIn) Intent(this, MyPageActivity::class.java)
                        else Intent(this, LoginPageActivity::class.java).also {
                            Toast.makeText(this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                        }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    //  MainActivity와 동일한 방식으로 데이터 로딩
    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(
                call: Call<FoodResponse>,
                response: Response<FoodResponse>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()?.getFoodkr?.item?.filter { !it.thumb.isNullOrEmpty() }
                        ?.distinctBy { it.Lat to it.Lng } ?: emptyList()
                    foodList = list
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                Toast.makeText(this@MyPageActivity, "데이터 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
