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
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding
import bitc.fullstack502.project2.FoodResponse
import bitc.fullstack502.project2.model.Item
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class MainActivity : AppCompatActivity() {

    // API 키값
    private val servicekey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
    private var foodList: List<FoodItem>? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val sliderHandler = Handler(Looper.getMainLooper())
    private var sliderPosition = 0
    private val sliderRunnable = object : Runnable {
        override fun run() {
            binding.viewPager.adapter?.let {
                val itemCount = it.itemCount
                sliderPosition = (sliderPosition + 1) % itemCount
                binding.viewPager.currentItem = sliderPosition
                sliderHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 슬라이더 데이터 세팅
        val sliderImages = listOf(
            R.drawable.sample1,
            R.drawable.sample2,
            R.drawable.sample3
        )
        binding.viewPager.adapter = SliderAdapter(sliderImages)

        // 자동 슬라이드 시작
        sliderHandler.postDelayed(sliderRunnable, 3000)

        // 2. 첫 번째 가로 리스트 데이터 세팅
        val horizontalItems = listOf(
            Item("Title 1", 4.5, "Category", "Addr"),
            Item("Title 2", 4.5, "Category", "Addr"),
            Item("Title 3", 4.5, "Category", "Addr")
        )
        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = HorizontalAdapter(horizontalItems)

        // 3. 두 번째 가로 리스트 데이터 세팅
        val horizontalItems2 = listOf(
            Item("Title A", 3.5, "Category", "Addr"),
            Item("Title B", 4.0, "Category", "Addr"),
            Item("Title C", 5.0, "Category", "Addr")
        )
        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(horizontalItems2)

        // 4. 세로 리스트 데이터 세팅
        val verticalItems = List(5) { Item("Title $it", 4.5, "Category", "Addr") }
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = VerticalAdapter(verticalItems)

        // 5. 하단 네비게이션 클릭 이벤트 처리 (로그인 체크 통합)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> Log.d("MainActivity", "home selected")
                R.id.menu_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                }
                R.id.menu_favorite -> Log.d("MainActivity", "favorite selected")
                R.id.menu_profile -> {
                    if (isLoggedIn()) {
                        // 로그인 되어 있으면 MyPageActivity로 이동
                        val intent = Intent(this, MyPageActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 로그인 안 되어 있으면 LoginActivity로 이동
                        val intent = Intent(this, LoginPageActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }
        setupButtonListeners()
        setupSlider()
        setupRecyclerViews()
        setupBottomNavigation()
        fetchFoodData()
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey = servicekey)
            .enqueue(object : Callback<FoodResponse> {

                override fun onResponse(
                    call: Call<FoodResponse>,
                    response: Response<FoodResponse>
                ) {
                    if (response.isSuccessful) {
                        val foodList = response.body()?.getFoodkr?.item
                        val originalFoodList = response.body()?.getFoodkr?.item
                        if (!foodList.isNullOrEmpty()) {


                            val listWithImages = originalFoodList?.filter { it.image != null }

                            // 2단계: 이미지가 있는 목록을 기반으로 좌표 중복을 제거합니다.

                            this@MainActivity.foodList = listWithImages

                        } else {
                            Log.d("MainActivity", "데이터가 없습니다.")
                            Toast.makeText(this@MainActivity, "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e("MainActivity", "서버 응답 오류: ${response.code()}")
                        Toast.makeText(this@MainActivity, "서버에서 응답을 받지 못했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("MainActivity", "API 호출 실패", t)
                }
            })
    }

    private fun setupSlider() {
        val sliderImages = listOf(R.drawable.sample1, R.drawable.sample2, R.drawable.sample3)
        binding.viewPager.adapter = SliderAdapter(sliderImages)
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun setupRecyclerViews() {
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verticalRecyclerView.adapter = VerticalAdapter(emptyList()) // ⚡ Context 제거

        binding.horizontalRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView.adapter = HorizontalAdapter(emptyList())

        binding.horizontalRecyclerView2.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(emptyList())
    }

    private fun setupButtonListeners() {
        binding.btnFav.setOnClickListener {
            if (!foodList.isNullOrEmpty()) {
                val currentItem = foodList!![0]

                val randomSubList = foodList!!.shuffled().take(40)
                val intent = Intent(this, FavoritesActivity::class.java).apply {
                    putExtra("clicked_item", currentItem)
                    putParcelableArrayListExtra("full_list", ArrayList(randomSubList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "데이터가 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
                R.id.menu_list -> true
                R.id.menu_favorite -> true
                R.id.menu_profile -> {
                    val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                    if (isLoggedIn) startActivity(Intent(this, MyPageActivity::class.java))
                    else startActivity(Intent(this, LoginPageActivity::class.java))
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
                    val list = response.body()?.getFoodkr?.item
                    if (!list.isNullOrEmpty()) {
                        val itemList = list.map {
                            Item(
                                title = it.MAIN_TITLE ?: "이름 없음",
                                rating = 0.0,
                                category = it.CATE_NM ?: "메뉴 정보 없음",
                                address = it.ADDR ?: "주소 없음",
                                thumbUrl = it.MAIN_IMG ?: ""
                            )
                        }
                        foodList = itemList

                        // 전체 장소는 5개만
                        binding.verticalRecyclerView.adapter = VerticalAdapter(itemList.take(5))

                        // 추천 / 신규 장소 5개씩
                        binding.horizontalRecyclerView.adapter = HorizontalAdapter(itemList.shuffled().take(5))
                        binding.horizontalRecyclerView2.adapter = HorizontalAdapter(itemList.shuffled().take(5))

                    } else {
                        Toast.makeText(this@MainActivity, "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
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
}
