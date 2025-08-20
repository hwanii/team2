package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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

    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
    private var foodList: List<FoodItem> = emptyList()
    private var currentFilteredList: List<FoodItem> = emptyList()
    private var currentGu: String = "전체"

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var verticalAdapter: VerticalAdapter

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

    private val guList = listOf("전체", "부산진구", "북구", "해운대구")
    private var selectedGuButton: Button? = null

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
        setupBottomNavigation()
        setupVerticalAdapter()
        setupGuFilterButtons()
        fetchFoodData()

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    // ===================== VerticalAdapter =====================
    private fun setupVerticalAdapter() {
        verticalAdapter = VerticalAdapter(object : VerticalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                startActivity(Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("item", item)
                })
            }

            override fun onLoadMore() {
                verticalAdapter.addMore()
            }
        })
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = verticalAdapter
    }

    private fun filterByGu(gu: String) {
        currentGu = gu
        currentFilteredList = if (gu == "전체") {
            foodList
        } else {
            foodList.filter { it.ADDR?.contains(gu) == true }
        }

        verticalAdapter.setFullList(currentFilteredList)
    }

    // ===================== 구 버튼 =====================
    private fun setupGuFilterButtons() {
        val guLayout = binding.guFilterLayout
        guLayout.removeAllViews()
        guList.forEach { gu ->
            val button = Button(this).apply {
                text = gu
                setPadding(36, 16, 36, 16)
                setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                setTextColor(resources.getColor(android.R.color.white, null))

                setOnClickListener {
                    filterByGu(gu)
                    // 이전 선택 해제
                    selectedGuButton?.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                    // 현재 선택 강조
                    setBackgroundColor(resources.getColor(android.R.color.holo_orange_light, null))
                    selectedGuButton = this
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(8, 0, 8, 0)
            guLayout.addView(button, lp)

            // 기본 선택: 전체
            if (gu == "전체") {
                button.performClick()
            }
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
                    foodList = rawList.filter { !it.thumb.isNullOrBlank() }
                        .distinctBy { it.Lat to it.Lng }

                    // HorizontalAdapter 세팅
                    setupHorizontalAdapters()

                    // VerticalAdapter에 전체 데이터 세팅
                    filterByGu("전체")
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun setupHorizontalAdapters() {
        val recommendList = foodList.take(5)
        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = HorizontalAdapter(recommendList, object :
            HorizontalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                startActivity(Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("item", item)
                })
            }
        })

        val newList = foodList.takeLast(5)
        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(newList, object :
            HorizontalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                startActivity(Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("item", item)
                })
            }
        })
    }

    // ===================== 슬라이더 =====================
    private fun setupSlider() {
        val slides = listOf(
            SlideItem(R.drawable.slide1, "부산 맛집 검색", ""),
            SlideItem(R.drawable.slide2, "부산 조개구이 검색", ""),
            SlideItem(R.drawable.slide3, "부산 야장 검색", ""),
            SlideItem(R.drawable.slide4, "부산 고기집 검색", "")
        )
        binding.viewPager.adapter = SliderAdapter(slides, this)
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    // ===================== 바텀 네비 =====================
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

    private fun fetchFoodData() {
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(
                call: Call<FoodResponse>,
                response: Response<FoodResponse>
            ) {
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
                    binding.horizontalRecyclerView.adapter =
                        HorizontalAdapter(itemList.shuffled().take(5))
                    binding.horizontalRecyclerView2.adapter =
                        HorizontalAdapter(itemList.shuffled().take(5))
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "서버에서 응답을 받지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@MainActivity,
                    "데이터를 불러오는 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }
}
