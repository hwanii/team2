package bitc.fullstack502.project2

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
import bitc.fullstack502.project2.Adapter.SliderAdapter
import bitc.fullstack502.project2.Adapter.VerticalAdapter
import bitc.fullstack502.project2.databinding.ActivityMainBinding
import bitc.fullstack502.project2.model.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    // API 키값
    private val servicekey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    private var foodList: List<FoodItem>? = null

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // 자동 슬라이드용 핸들러와 인덱스 변수
    private val sliderHandler = Handler(Looper.getMainLooper())
    private var sliderPosition = 0

    // 자동 슬라이드 실행 함수
    private val sliderRunnable = object : Runnable {
        override fun run() {
            val adapter = binding.viewPager.adapter
            if (adapter != null) {
                val itemCount = adapter.itemCount
                sliderPosition = (sliderPosition + 1) % itemCount
                binding.viewPager.currentItem = sliderPosition
                sliderHandler.postDelayed(this, 3000) // 3초마다 슬라이드
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 시스템 바 패딩 적용
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
                R.id.menu_search -> Log.d("MainActivity", "search selected")
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
        fetchFoodData()

        // 6. 디테일 버튼 클릭 이벤트
//        binding.btnDetail.setOnClickListener {
//            item?.let {
//                val intent = Intent(this, DetailActivity::class.java)
//                intent.putExtra("title", it.TITLE)
//                intent.putExtra("addr", it.ADDR)
//                intent.putExtra("subaddr", it.SubAddr)
//                intent.putExtra("tel", it.TEL)
//                intent.putExtra("time", it.Time)
//                intent.putExtra("item", it.Item)
//                intent.putExtra("imageurl", it.image)
//                intent.putExtra("lat", it.Lat ?: 0.0f)
//                intent.putExtra("lng", it.Lng ?: 0.0f)
//                intent.putExtra("gugun", it.GUGUN_NM)
//                startActivity(intent)
//            } ?: run {
//                Toast.makeText(this, "데이터가 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티 종료 시 핸들러 콜백 제거
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey = servicekey)
            .enqueue(object : Callback<FoodResponse> {
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        val foodList = response.body()?.getFoodkr?.item
                        if (!foodList.isNullOrEmpty()) {

                            this@MainActivity.foodList = response.body()?.getFoodkr?.item

                        } else {
                            Log.d("MainActivity", "데이터가 없습니다.")
                            Toast.makeText(this@MainActivity, "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e("MainActivity", "서버 응답 오류: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("MainActivity", "API 호출 실패", t)
                }
            })
    }

    // 로그인 상태 확인 함수 (SharedPreferences 사용)
    private fun isLoggedIn(): Boolean {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        // "logged_in" key 값이 true이면 로그인 상태, 없거나 false면 미로그인
        return prefs.getBoolean("logged_in", false)
    }

    private fun setupButtonListeners() {
        binding.btnDetail.setOnClickListener {
            if (!foodList.isNullOrEmpty()) {
                val currentItem = foodList!![0]

                val randomSubList = foodList!!.shuffled().take(40)
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("clicked_item", currentItem)
                    putParcelableArrayListExtra("full_list", ArrayList(randomSubList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "데이터가 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
            }
//        binding.btnList.setOnClickListener {
//            val intent = Intent(this, ListActivity::class.java)
//            startActivity(intent)
        }
    }
}
