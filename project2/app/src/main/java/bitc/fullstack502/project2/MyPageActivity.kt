package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMyPageBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyPageActivity : AppCompatActivity() {
    
    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
    
    private lateinit var binding: ActivityMyPageBinding
    private lateinit var user: User
    private lateinit var reviewContainer: LinearLayout
    
    // 리뷰에 매칭된 FoodItem을 저장할 리스트
    private val allFoodList = mutableListOf<FoodItem>()
    
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedUser = result.data?.getParcelableExtra<User>("updatedUser")
            if (updatedUser != null) {
                user = updatedUser
                updateUI(user)
                saveToPrefs(user)
                loadUserReviews(user.userKey)
            }
        }
    }
    
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
        
        reviewContainer = binding.myReview.findViewById(R.id.review_container)
        
        // SharedPreferences에서 사용자 정보 불러오기
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        user = User(
            userKey = prefs.getInt("user_key", 0),
            userName = prefs.getString("user_name", "") ?: "",
            userId = prefs.getString("user_id", "") ?: "",
            userPw = prefs.getString("user_pw", "") ?: "",
            userTel = prefs.getString("user_tel", "") ?: "",
            userEmail = prefs.getString("user_email", "") ?: ""
        )
        
        updateUI(user)
        
        binding.goEdit.setOnClickListener {
            val intent = Intent(this, EditPageActivity::class.java)
            intent.putExtra("user", user)
            editLauncher.launch(intent)
        }
        
        binding.logoutButton.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }
        
        loadAllFood()
        setupBottomNavigation()
    }
    
    private fun loadAllFood() {
        RetrofitClient.api.getFoodList(serviceKey = serviceKey, ucSeq = null)
            .enqueue(object : Callback<FoodResponse> {
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        allFoodList.clear()
                        response.body()?.getFoodkr?.item?.let { allFoodList.addAll(it) }
                        
                        if (user.userKey != 0) {
                            loadUserReviews(user.userKey)
                        }
                    }
                }
                
                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Toast.makeText(this@MyPageActivity, "음식 데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
    
    private fun updateUI(user: User) {
        binding.userName.text = "이름 : ${user.userName}"
        binding.userId.text = "ID : ${user.userId}"
    }
    
    private fun saveToPrefs(user: User) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("user_key", user.userKey)
            putString("user_name", user.userName)
            putString("user_id", user.userId)
            putString("user_pw", user.userPw)
            putString("user_tel", user.userTel)
            putString("user_email", user.userEmail)
        }.apply()
    }
    
    private fun loadUserReviews(userKey: Int) {
        if (userKey == 0) return
        
        reviewContainer.removeAllViews()
        binding.myReview.visibility = View.GONE
        binding.noReview.visibility = View.GONE
        
        RetrofitClient.reviewApi.getUserReviews(userKey)
            .enqueue(object : Callback<List<ReviewResponse>> {
                override fun onResponse(
                    call: Call<List<ReviewResponse>>,
                    response: Response<List<ReviewResponse>>
                ) {
                    if (response.isSuccessful) {
                        val reviews = response.body() ?: emptyList()
                        
                        if (reviews.isEmpty()) {
                            binding.noReview.visibility = View.VISIBLE
                        } else {
                            binding.myReview.visibility = View.VISIBLE
                            
                            for (review in reviews) {
                                val item = layoutInflater.inflate(
                                    R.layout.item_review,
                                    reviewContainer,
                                    false
                                )
                                
                                // 텍스트 설정
                                item.findViewById<TextView>(R.id.review_item).text = review.reviewItem
                                item.findViewById<TextView>(R.id.review_num).text = review.reviewRating.toString()
                                
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val dateTime = LocalDateTime.parse(review.reviewDay)
                                val formattedDate = dateTime.format(formatter)
                                item.findViewById<TextView>(R.id.review_day).text = formattedDate
                                
                                // 이미지 설정
                                val reviewImageView = item.findViewById<ImageView>(R.id.review_image)
                                val clickedFood = allFoodList.find { it.UcSeq == review.placeCode }
                                val imageUrl = clickedFood?.image ?: clickedFood?.thumb
                                
                                if (!imageUrl.isNullOrEmpty()) {
                                    Glide.with(this@MyPageActivity)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.heart_full)
                                        .error(R.drawable.heart_none)
                                        .into(reviewImageView)
                                } else {
                                    reviewImageView.setImageResource(R.drawable.heart_none)
                                }
                                
                                // 삭제 버튼
                                val deleteBtn = item.findViewById<ImageView>(R.id.delete_btn)
                                deleteBtn.setOnClickListener {
                                    RetrofitClient.reviewApi.deleteReview(review.reviewKey)
                                        .enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    Toast.makeText(this@MyPageActivity, "삭제 완료", Toast.LENGTH_SHORT).show()
                                                    loadUserReviews(userKey)
                                                }
                                            }
                                            
                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                Toast.makeText(this@MyPageActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                }
                                
                                // 리뷰 클릭 -> DetailActivity 이동
                                item.setOnClickListener {
                                    if (clickedFood != null) {
                                        val intent = Intent(this@MyPageActivity, DetailActivity::class.java)
                                        intent.putExtra("clicked_item", clickedFood)
                                        intent.putParcelableArrayListExtra("full_list", ArrayList(allFoodList))
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this@MyPageActivity, "유효하지 않은 placeCode", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                
                                reviewContainer.addView(item)
                            }
                        }
                    } else {
                        Toast.makeText(this@MyPageActivity, "리뷰 불러오기 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                    Toast.makeText(this@MyPageActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
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
                    if (!allFoodList.isNullOrEmpty()) {
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra(
                                "full_list",
                                ArrayList(allFoodList) // 전체 음식 리스트 전달
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
