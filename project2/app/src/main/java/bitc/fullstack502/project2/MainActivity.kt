package bitc.fullstack502.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.HorizontalAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding
import bitc.fullstack502.project2.FoodResponse
import bitc.fullstack502.project2.databinding.ActivityFavoritesBinding
import bitc.fullstack502.project2.model.Item
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class MainActivity : AppCompatActivity() {

    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    private var foodList: List<FoodItem>? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val sliderHandler = Handler(Looper.getMainLooper())
    private var sliderPosition = 0
    private val sliderRunnable = object : Runnable {
        override fun run() {
            binding.viewPager.adapter?.let {
                sliderPosition = (sliderPosition + 1) % it.itemCount
                binding.viewPager.currentItem = sliderPosition
                sliderHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)



        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        setupSlider()
        setupRecyclerViews()
        setupBottomNavigation()

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))

        }
        fetchFoodData()
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    private fun setupSlider() {
        val sliderImages = listOf(R.drawable.sample1, R.drawable.sample2, R.drawable.sample3)
        binding.viewPager.adapter = SliderAdapter(sliderImages)
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun setupRecyclerViews() {
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = VerticalAdapter(emptyList())

        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = HorizontalAdapter(emptyList())

        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(emptyList())
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
                R.id.menu_list -> true
//                db 연동 하기 전 임시 테스트 로직입니다.
                R.id.menu_favorite -> {
                    if (!foodList.isNullOrEmpty()) {
                        // API로 가져온 실제 데이터 중 앞 3개를 임시 즐겨찾기 데이터로 선택
                        val mockFavorites = foodList!!.shuffled().take(6)

                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            // "favorites_list" 라는 이름으로 임시 목록 전달
                            putParcelableArrayListExtra("favorites_list", ArrayList(mockFavorites))
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "아직 데이터 로딩 중입니다.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_profile -> {
                    val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                    val intent = if (isLoggedIn) Intent(this, MyPageActivity::class.java)
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

    private fun fetchFoodData() {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val list = response.body()?.getFoodkr?.item?.filter { !it.thumb.isNullOrEmpty() }
                        ?.distinctBy { it.Lat to it.Lng } ?: emptyList()

                    val filteredList = list.filter { !it.thumb.isNullOrEmpty() }
                        .distinctBy { it.Lat to it.Lng }
                    foodList = filteredList

                    val itemList = list.map {
                        Item(
                            title = it.MAIN_TITLE ?: "이름 없음",
                            rating = 0.0,
                            category = it.CATE_NM ?: "메뉴 정보 없음",
                            address = it.ADDR ?: "주소 없음",
                            thumbUrl = it.thumb ?: ""
                        )
                    }


                    binding.verticalRecyclerView.adapter = VerticalAdapter(itemList.take(5))
                    binding.horizontalRecyclerView.adapter = HorizontalAdapter(itemList.shuffled().take(5))
                    binding.horizontalRecyclerView2.adapter = HorizontalAdapter(itemList.shuffled().take(5))

                } else {
                    Toast.makeText(this@MainActivity, "서버에서 응답을 받지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }
}