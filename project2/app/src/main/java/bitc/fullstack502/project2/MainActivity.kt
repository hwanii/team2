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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.HorizontalAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.Adapter.SlideItem
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.RetrofitClient.reviewApi
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
    
    private var currentUserKey: Int = 0
    
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
    // BottomNavigationView 숨김/반투명 관련
    // ============================
    private val navHandler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null
    
    // 기본/스크롤 상태 배경색
    private val navColorDefault by lazy { ContextCompat.getColor(this, R.color.button_unselected) }
    private val navColorTransparent = Color.TRANSPARENT
    
    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        // 시스템 바 inset 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // ============================
        // BottomNavigationView 스크롤 숨김/반투명 처리
        // ============================
        binding.nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            // 스크롤 중일 때: 네비 숨김 & 완전 투명
            binding.bottomNavigationView.visibility = View.GONE
            binding.bottomNavigationView.setBackgroundColor(Color.TRANSPARENT) // 스크롤 중
            
            
            // 기존 예약된 Runnable 취소
            hideRunnable?.let { navHandler.removeCallbacks(it) }
            
            // 스크롤 멈춤 후 1초 뒤: 네비 등장 & 반투명 배경
            hideRunnable = Runnable {
                binding.bottomNavigationView.visibility = View.VISIBLE
                binding.bottomNavigationView.setBackgroundColor(Color.parseColor("#CCFFFFFF")) // 멈췄을 때
                
            }
            navHandler.postDelayed(hideRunnable!!, 1000)
        }
        
        // ============================
        // 초기화
        // ============================
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
    
    override fun onResume() {
        super.onResume()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
        hideRunnable?.let { navHandler.removeCallbacks(it) }
    }

    // ============================
// VerticalAdapter 초기화
// RecyclerView 세로 스크롤용
// 클릭 이벤트 처리 + 리뷰 API 연동
// ============================
    private fun setupVerticalAdapter() {
        // 어댑터 생성: listener와 reviewApi 주입
        verticalAdapter = VerticalAdapter(
            listener = object : VerticalAdapter.ItemClickListener {
                override fun onItemClick(item: FoodItem) {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                        putExtra("clicked_item", item) // 클릭된 아이템 전달
                        // 🔹 전체 리스트도 같이 전달 (추천 가게 계산용)
                        putParcelableArrayListExtra("full_list", ArrayList(foodList))
                    }
                    startActivity(intent)
                }

                override fun onLoadMore() {
                    verticalAdapter.addMore()
                }
            },
            reviewApi = reviewApi
        )


        // RecyclerView 레이아웃 매니저 설정 (세로 스크롤)
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 연결
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
        // 프로그레스 바 표시
        binding.progressBar.visibility = View.VISIBLE

        // Retrofit으로 음식 데이터 호출
        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                // 로딩 끝나면 프로그레스 바 숨김
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val body = response.body()
                    val rawList = body?.getFoodkr?.item ?: emptyList()

                    // 필터링 과정
                    foodList = rawList.filter {
                        // 이미지가 있고, 좌표가 null/0이 아닌 항목만 남김
                        !it.thumb.isNullOrBlank() && it.Lat != null && it.Lng != null && it.Lat != 0f && it.Lng != 0f
                    }.distinctBy { it.Lat to it.Lng }

                    // HorizontalAdapter 초기화
                    setupHorizontalAdapters()

                    // VerticalAdapter에 전체 데이터 세팅 (기본 필터: 전체)
                    filterByGu("전체")
                }
            }

            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                // 실패 시 프로그레스 바 숨기고 토스트 표시
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
                    putExtra("clicked_item", item)
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
                    putExtra("clicked_item", item)
                }
                startActivity(intent)
            }
        })
        
        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = cafeAdapter
    }
    
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
                    if (!foodList.isNullOrEmpty()) {
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
                    val currentUserKey = prefs.getInt("user_key", 0)
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

                    intent.putExtra("user_key", currentUserKey)

                    //  foodList 같이 넘기기
                    if (!foodList.isNullOrEmpty()) {
                        intent.putParcelableArrayListExtra(
                            "full_list",
                            ArrayList(foodList)
                        )
                    }

                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
