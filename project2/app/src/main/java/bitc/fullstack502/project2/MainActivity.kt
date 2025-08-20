package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.HorizontalAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.Adapter.SlideItem
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
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
        setupVerticalAdapter()
        setupGuFilterButtons()

        binding.btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        fetchFoodData(
            onSuccess = { list ->
                setupHorizontalAdapters()
                filterByGu(currentGu)
            },
            onError = {
                Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )


        val bottomNavView = binding.baseLayout.bottomNavigationView
        setupBottomNavigation(bottomNavView)
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



    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getBoolean("isLoggedIn", false)
    }
}
