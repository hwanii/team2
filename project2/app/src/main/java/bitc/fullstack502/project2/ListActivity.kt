package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.databinding.ActivityListBinding
import bitc.fullstack502.project2.model.FavoriteItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ListActivity : AppCompatActivity() {
    
    private val binding by lazy { ActivityListBinding.inflate(layoutInflater) }
    private val serviceKey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
    
    private val foodList = mutableListOf<FoodItem>()
    private val originalList = mutableListOf<FoodItem>()
    private lateinit var adapter: PlaceAdapter
    private val categorySet = mutableSetOf<String>()
    private var selectedButton: FilterButton? = null
    private var currentUserKey: Int = 0
    private val favoritePlaceCodes = mutableSetOf<Int>() // 서버에서 받은 즐겨찾기 placeCode
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        currentUserKey = intent.getIntExtra("user_key", 0)
        
        // 어댑터 초기화
        adapter = PlaceAdapter(
            foodList,
            itemClickListener = { foodItem ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("clicked_item", foodItem)
                    putParcelableArrayListExtra("full_list", ArrayList(originalList))
                }
                startActivity(intent)
            },
            onBookmarkClick = { item, isBookmarked ->
                val body = mapOf(
                    "userKey" to currentUserKey,
                    "placeCode" to item.UcSeq
                )
                
                val call = if (isBookmarked) {
                    RetrofitClient.favoritesApi.addFavorite(body)
                } else {
                    RetrofitClient.favoritesApi.removeFavorite(body)
                }
                
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (!response.isSuccessful) {
                            Toast.makeText(
                                this@ListActivity,
                                "즐겨찾기 업데이트 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                            item.isBookmarked = !isBookmarked
                            adapter.notifyItemChanged(foodList.indexOf(item))
                        } else {
                            // 서버에서 다시 즐겨찾기 상태 가져오기
                            fetchUserFavorites()
                        }
                    }
                    
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ListActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                        item.isBookmarked = !isBookmarked
                        adapter.notifyItemChanged(foodList.indexOf(item))
                    }
                })
            }
        )
        
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = adapter
        
        // 리스트 구분선
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.divider_custom)?.let { divider.setDrawable(it) }
        binding.rvList.addItemDecoration(divider)
        
        // 초기 전체 리스트 로딩
        fetchListData()
        
        setupBottomNavigation()
        
        // 검색창
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterList(s?.toString())
            }
        })
        
        // 이전 버튼
        binding.btnBack.setOnClickListener { finish() }
    }
    
    private fun fetchUserFavorites() {
        RetrofitClient.favoritesApi.getFavorites(currentUserKey)
            .enqueue(object : Callback<List<Int>> {
                override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                    if (response.isSuccessful) {
                        // 서버에서 받은 placeCode 리스트
                        val favoritePlaceCodesFromServer = response.body() ?: emptyList()
                        
                        // 0은 제외하고 Set에 담기 (타입 안전)
                        favoritePlaceCodes.clear()
                        favoritePlaceCodes.addAll(favoritePlaceCodesFromServer.filter { it != 0 })
                        
                        Log.d("FavoritesDebug", "favoritePlaceCodes=$favoritePlaceCodes")
                        
                        // 원본 + 현재 리스트 모두 반영
                        originalList.forEach { it.isBookmarked = favoritePlaceCodes.contains(it.UcSeq.toInt()) }
                        foodList.forEach { it.isBookmarked = favoritePlaceCodes.contains(it.UcSeq.toInt()) }
                        
                        // 각 아이템 상태 로그 찍어보기
                        foodList.forEach {
                            Log.d(
                                "FavoritesDebug",
                                "UcSeq=${it.UcSeq}, isBookmarked=${it.isBookmarked}"
                            )
                        }
                        
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("ListActivity", "즐겨찾기 로딩 실패: ${response.code()}")
                    }
                }
                
                override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                    Log.e("ListActivity", "즐겨찾기 API 호출 실패", t)
                }
            })
    }
    
    private fun fetchListData() {
        RetrofitClient.api.getFoodList(serviceKey = serviceKey, title = null, gugun = null)
            .enqueue(object : Callback<FoodResponse> {
                override fun onResponse(
                    call: Call<FoodResponse>,
                    response: Response<FoodResponse>
                ) {
                    if (response.isSuccessful) {
                        val items = response.body()?.getFoodkr?.item ?: emptyList()
                        originalList.clear()
                        categorySet.clear()
                        items.forEach {
                            categorySet.add(it.GUGUN_NM)
                            originalList.add(it)
                        }
                        updateList(null)
                        createFilterButtons()
                        
                        // 리스트 데이터가 준비된 후 즐겨찾기 상태 반영
                        fetchUserFavorites()
                    } else {
                        Toast.makeText(
                            this@ListActivity,
                            "서버 응답 오류: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                
                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("ListActivity", "API 호출 실패", t)
                    Toast.makeText(this@ListActivity, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
    
    private fun createFilterButtons() {
        val container = binding.filterBtnContainer
        container.removeAllViews()
        val desiredOrder = listOf(
            "해운대구", "기장군", "수영구", "남구", "부산진구", "동구", "중구", "서구",
            "영도구", "연제구", "동래구", "금정구", "북구", "사상구", "강서구", "사하구"
        )
        val sortedCategories: List<String> = desiredOrder.filter { categorySet.contains(it) }
        
        val allButton = FilterButton(this).apply {
            text = "부산 전체"
            setOnStyle()
            selectedButton = this
            setOnClickListener {
                updateList(null)
                updateSelectedButton(this, container)
            }
        }
        container.addView(allButton)
        
        sortedCategories.forEach { category ->
            val button = FilterButton(this).apply {
                text = category
                setOffStyle()
                setOnClickListener {
                    updateList(category)
                    updateSelectedButton(this, container)
                }
            }
            container.addView(button)
        }
    }
    
    private fun updateSelectedButton(newButton: FilterButton, container: LinearLayout) {
        selectedButton?.setOffStyle()
        newButton.setOnStyle()
        selectedButton = newButton
    }
    
    private fun updateList(filter: String?) {
        foodList.clear()
        if (filter.isNullOrEmpty()) foodList.addAll(originalList)
        else foodList.addAll(originalList.filter { it.GUGUN_NM == filter })
        
        // 즐겨찾기 상태 반영
        foodList.forEach { it.isBookmarked = favoritePlaceCodes.contains(it.UcSeq) }
        adapter.notifyDataSetChanged()
        binding.rvList.scrollToPosition(0)
    }
    
    private fun filterList(query: String?) {
        val searchText = query?.trim()?.lowercase() ?: ""
        foodList.clear()
        if (searchText.isEmpty()) foodList.addAll(originalList)
        else foodList.addAll(originalList.filter {
            it.TITLE.lowercase().contains(searchText) ||
              (it.CATE_NM ?: "").lowercase().contains(searchText)
        })
        
        // 검색 후 즐겨찾기 상태 반영
        foodList.forEach { it.isBookmarked = favoritePlaceCodes.contains(it.UcSeq) }
        adapter.notifyDataSetChanged()
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