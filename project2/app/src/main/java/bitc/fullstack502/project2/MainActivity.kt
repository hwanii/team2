package bitc.fullstack502.project2

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
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

    // ============================
    // API KEY
    // ============================
    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    // ============================
    // 데이터 저장
    // ============================
    private var foodList: List<FoodItem> = emptyList()            // 전체 데이터
    private var currentFilteredList: List<FoodItem> = emptyList() // 현재 선택 구 기준

    // HorizontalAdapter (대표 메뉴, 감성 카페)
    private lateinit var recommendAdapter: HorizontalAdapter
    private lateinit var cafeAdapter: HorizontalAdapter

    // 구 필터 버튼 관련
    private val guList = listOf("전체", "부산진구", "북구", "해운대구")
    private var selectedGuButton: Button? = null
    private var currentGu: String = "전체"

    // VerticalAdapter (세로)
    private lateinit var verticalAdapter: VerticalAdapter

    // ViewBinding
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // ============================
    // 슬라이더 관련
    // ============================
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

    // ============================
    // onCreate
    // ============================
    override fun onCreate(savedInstanceState: Bundle?) {

        // MainActivity onCreate() 안이나 초기화 코드에 추가
        val hideHandler = Handler(Looper.getMainLooper())
        var hideRunnable: Runnable? = null

        binding.nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            // 스크롤 중일 때 BottomNavigationView 숨김
            binding.bottomNavigationView.visibility = View.GONE

            // 기존 예약된 Runnable 취소
            hideRunnable?.let { hideHandler.removeCallbacks(it) }

            // 스크롤 멈춤 후 1초 뒤 BottomNavigationView 표시
            hideRunnable = Runnable {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
            hideHandler.postDelayed(hideRunnable!!, 1000) // 1초
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 시스템 바 inset 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 초기화
        setupSlider()
        setupBottomNavigation()
        setupVerticalAdapter()
        setupGuFilterButtons()
        fetchFoodData() // API 호출

        // 검색 버튼 클릭
        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    // ============================
    // onResume: 앱 복귀 시 HorizontalAdapter 갱신
    // ============================
    override fun onResume() {
        super.onResume()
        // 다른 페이지 다녀오고 돌아올 때 최신 데이터 가져오기
        // 클릭 이벤트에서는 절대 호출하지 않음

    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    // ============================
    // VerticalAdapter 초기화
    // ============================
    private fun setupVerticalAdapter() {
        verticalAdapter = VerticalAdapter(object : VerticalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)  // 여기 키를 DetailActivity와 맞춰야 함
                }
                startActivity(intent)
            }

            override fun onLoadMore() {
                verticalAdapter.addMore() // 무한 스크롤
            }
        })
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = verticalAdapter
    }

    // ============================
    // 구 필터 적용
    // ============================
    private fun filterByGu(gu: String) {
        currentGu = gu
        currentFilteredList = if (gu == "전체") foodList
        else foodList.filter { it.ADDR?.contains(gu) == true }

        verticalAdapter.setFullList(currentFilteredList)
    }

    // ============================
    // 구 버튼 생성
    // ============================
    private fun setupGuFilterButtons() {
        val guLayout = binding.guFilterLayout
        guLayout.removeAllViews()

        guList.forEach { gu ->
            val button = Button(this).apply {
                text = gu
                textSize = 14f
                isAllCaps = false
                setTextColor(resources.getColor(R.color.button_text, null))
                background = ContextCompat.getDrawable(context, R.drawable.rounded_button)
                setPadding(36, 16, 36, 20)
                minHeight = 48

                setOnClickListener {
                    filterByGu(gu)

                    // 이전 선택 해제
                    selectedGuButton?.background =
                        ContextCompat.getDrawable(context, R.drawable.rounded_button)
                    selectedGuButton?.setTextColor(resources.getColor(R.color.button_text, null))

                    // 현재 선택 강조
                    background = ContextCompat.getDrawable(context, R.drawable.rounded_button_selected)
                    setTextColor(resources.getColor(R.color.button_text, null))
                    selectedGuButton = this
                }
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = if (gu == guList.first()) 16 else 8
                rightMargin = if (gu == guList.last()) 16 else 8
                topMargin = 8
                bottomMargin = 8
            }

            guLayout.addView(button, lp)

            if (gu == "전체") button.performClick()
        }
    }

    // ============================
    // API 호출
    // ============================
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

    // ============================
// HorizontalAdapter 세팅
// ============================
    private fun setupHorizontalAdapters() {

        // ----------------- 대표 메뉴 -----------------
        val recommendList = foodList
            .filter { it.CATE_NM?.contains("밀면") == true
                    || it.CATE_NM?.contains("회") == true
                    || it.CATE_NM?.contains("국밥") == true }
            .shuffled()
            .take(5)
            .toMutableList()

        recommendAdapter = HorizontalAdapter(recommendList, object : HorizontalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)  // 여기 키를 DetailActivity와 맞춰야 함
                }
                startActivity(intent)
            }
        })

        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = recommendAdapter

        // ----------------- 감성 카페 -----------------
        val cafeList = foodList
            .filter { it.CATE_NM?.contains("카페") == true }
            .shuffled()
            .take(5)
            .toMutableList()

        cafeAdapter = HorizontalAdapter(cafeList, object : HorizontalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)  // 여기 키를 DetailActivity와 맞춰야 함
                }
                startActivity(intent)
            }
        })

        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = cafeAdapter
    }
    // ============================
    // HorizontalAdapter 갱신 (앱 복귀 시만 호출)
    // ============================
//    private fun refreshHorizontalAdapters() {
//        val newRecommendList = foodList
//            .filter { it.CATE_NM?.contains("밀면") == true
//                    || it.CATE_NM?.contains("회") == true
//                    || it.CATE_NM?.contains("국밥") == true }
//            .shuffled()
//            .take(5)
//
//        recommendAdapter.updateList(newRecommendList)
//
//        val newCafeList = foodList
//            .filter { it.CATE_NM?.contains("카페") == true }
//            .shuffled()
//            .take(5)
//
//        cafeAdapter.updateList(newCafeList)
//    }

    // ============================
    // 슬라이더
    // ============================
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

    // ============================
    // 바텀 네비게이션
    // ============================
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
                R.id.menu_list -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.menu_favorite -> {
                    if (foodList.isNotEmpty()) {
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
