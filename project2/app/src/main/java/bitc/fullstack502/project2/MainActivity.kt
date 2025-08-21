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
<<<<<<< HEAD
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
=======
>>>>>>> 2c2ca605530dd0ad924084ea254faf4ab30cb498
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.HorizontalAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.Adapter.SlideItem
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding

<<<<<<< HEAD
class MainActivity : AppCompatActivity() {

    // ============================
    // API KEY
    // ============================
    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
=======
class MainActivity : BaseActivity() {
    private var currentFilteredList: List<FoodItem> = emptyList()
    private var currentGu: String = "전체"
>>>>>>> 2c2ca605530dd0ad924084ea254faf4ab30cb498

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
    private var currentUserKey: Int = 0

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
        setupVerticalAdapter()
        setupGuFilterButtons()

        fetchFoodData(
            onSuccess = { list ->
                setupHorizontalAdapters()
                filterByGu(currentGu)
            },
            onError = {
                Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )
        val bottomNavView = binding.bottomNav.bottomNavigationView
        setupBottomNavigation(bottomNavView)
        val btnSearch = binding.topBar.btnSearch
        setupSearchButton(btnSearch)
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
    // ============================
    private fun setupVerticalAdapter() {
        verticalAdapter = VerticalAdapter(object : VerticalAdapter.ItemClickListener {
            override fun onItemClick(item: FoodItem) {
                val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)
                }
                startActivity(intent)
            }

            override fun onLoadMore() {
                verticalAdapter.addMore()
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



    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }
}
