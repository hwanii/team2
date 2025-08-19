package bitc.fullstack502.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.HorizontalAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.Adapter.SlideItem
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

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

        // 안전영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSlider()
        setupBottomNavigation()
        fetchFoodData()

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    private fun setupSlider() {
        val slides = listOf(
            SlideItem(R.drawable.slide1, "부산 맛집 검색", "https://search.naver.com/search.naver?query=부산+맛집"),
            SlideItem(R.drawable.slide2, "부산 조개구이 검색", "https://search.naver.com/search.naver?query=부산+조개구이"),
            SlideItem(R.drawable.slide3, "부산 야장 검색", "https://search.naver.com/search.naver?query=부산+야장"),
            SlideItem(R.drawable.slide4, "부산 고기집 검색", "https://search.naver.com/search.naver?query=부산+고기집")
        )

        binding.viewPager.adapter = SliderAdapter(slides, this)
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
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
                    val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
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

    private fun fetchFoodData() {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val rawList = response.body()?.getFoodkr?.item ?: emptyList()
                    val filteredList = rawList
                        .filter { !it.thumb.isNullOrBlank() }
                        .distinctBy { it.Lat to it.Lng }

                    foodList = filteredList

                    setupRecyclerViews(filteredList)

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

    private fun setupRecyclerViews(foodItems: List<FoodItem>) {
        // Vertical RecyclerView
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = VerticalAdapter(foodItems.take(5),
            object : VerticalAdapter.ItemClickListener {
                override fun onItemClick(item: FoodItem) {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("item", item)
                    intent.putParcelableArrayListExtra("full_list", ArrayList(foodItems))
                    startActivity(intent)
                }
            })

        // Horizontal RecyclerView 1
        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = HorizontalAdapter(foodItems.shuffled().take(5),
            object : HorizontalAdapter.ItemClickListener {
                override fun onItemClick(item: FoodItem) {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("item", item)
                    intent.putParcelableArrayListExtra("full_list", ArrayList(foodItems))
                    startActivity(intent)
                }
            })

        // Horizontal RecyclerView 2
        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(foodItems.shuffled().take(5),
            object : HorizontalAdapter.ItemClickListener {
                override fun onItemClick(item: FoodItem) {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("item", item)
                    intent.putParcelableArrayListExtra("full_list", ArrayList(foodItems))
                    startActivity(intent)
                }
            })
    }
}
