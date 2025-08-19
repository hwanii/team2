package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.data.Place
import bitc.fullstack502.project2.databinding.ActivityListBinding
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.widget.SearchView


class ListActivity : AppCompatActivity() {
  private val binding by lazy { ActivityListBinding.inflate(layoutInflater) }
  //    공공데이터 포탈 부산맛집 정보 서비스 api key값
  private val servicekey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"
  
  // jin 추가
  private val placeList = mutableListOf<Place>()
  private val originalList = mutableListOf<Place>()
  private lateinit var adapter: PlaceAdapter
  private val categorySet = mutableSetOf<String>()
  
  private var selectedButton: FilterButton? = null
  // jin 추가 end
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)

//        fetchFoodData()
    
    // jin 추가
    adapter = PlaceAdapter(placeList) { place ->
      val intent = Intent(this, DetailActivity::class.java).apply {
        putExtra("title", place.title)
        putExtra("addr", place.addr)
        putExtra("category", place.category)
        putExtra("menu", place.menu)
        putExtra("time", place.time)
        putExtra("imageurl", place.imageUrl)
      }
      startActivity(intent)
    }
    binding.rvList.layoutManager = LinearLayoutManager(this)
    binding.rvList.adapter = adapter
    
    // 리스트 구분선
    val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
    val drawable = ContextCompat.getDrawable(this, R.drawable.divider_custom)
    drawable?.let {
      divider.setDrawable(it)
    }
    binding.rvList.addItemDecoration(divider)
    
    // 초기 전체 리스트 로딩
    fetchListData()
    
    // 검색창
    binding.etSearch.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        filterList(s?.toString())
      }
    })
    
    // 이전 버튼
    binding.btnBack.setOnClickListener {
      finish() // 이전 화면으로 돌아가기
    }
    // jin 추가 end
    
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
  }
  
  
  //    현재 가지고 있는 값 (title,addr,subadder 등등 ) 을 DetailActivity 에 전달하는 역할,
//    각각의 UcSeq(가게식별코드)에 맞는 값을 부여하는 소스
  private fun navigateToDatail(item: FoodItem) {
    val intent = Intent(this, DetailActivity::class.java).apply {
      putExtra("title", item.TITLE)
      putExtra("addr", item.ADDR)
      putExtra("subaddr", item.SubAddr)
      putExtra("tel", item.TEL)
      putExtra("time", item.Time)
      putExtra("item", item.Item)
      putExtra("imageurl", item.image)
      putExtra("lat", item.Lat ?: 0.0f)
      putExtra("lng", item.Lng ?: 0.0f)
      putExtra("UcSeq", item.UcSeq)
    }
    startActivity(intent)
  }
  
  
  // jin 추가
  private fun fetchListData() {
    RetrofitClient.api.getFoodList(
      serviceKey = servicekey,
      title = null,
      gugun = null
    ).enqueue(object : Callback<FoodResponse> {
      override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
        if (response.isSuccessful) {
          val foodList = response.body()?.getFoodkr?.item ?: emptyList()
          originalList.clear()
          categorySet.clear()
          
          foodList.forEach { item ->
            categorySet.add(item.GUGUN_NM)
            originalList.add(
              Place(
                id = item.hashCode().toLong(),
                title = item.TITLE,
                rating = 0.0,
                addr = item.ADDR,
                category = item.GUGUN_NM,
                menu = item.CATE_NM ?: "정보 없음",
                time = item.Time ?: "정보 없음",
                imageUrl = item.image ?: ""
              )
            )
          }
          
          // 전체 리스트 출력
          updateList(null)
          
          // 버튼 생성 (최초 1회만)
          createFilterButtons()
        } else {
          Toast.makeText(this@ListActivity, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
      }
      
      override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
        Log.e("ListActivity", "API 호출 실패", t)
        Toast.makeText(this@ListActivity, "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
      }
    })
  }
  
  
  private fun createFilterButtons() {
    val container = binding.filterBtnContainer
    container.removeAllViews()
    
    val desiredOrder = listOf("해운대구", "기장군", "수영구", "남구", "부산진구", "동구", "중구", "서구", "영도구", "연제구", "동래구", "금정구", "북구", "사상구", "강서구", "사하구")
    val sortedCategories: List<String> = desiredOrder.filter { categorySet.contains(it) }
    
    
    // 전체 버튼
    val allButton = FilterButton(this).apply {
      text = "부산 전체"
      setOnStyle()
      selectedButton = this // 초기 선택 버튼
      setOnClickListener {
        updateList(null)
        updateSelectedButton(this, container)
      }
    }
    container.addView(allButton)
    
    // 각 구 버튼
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
  
  // 선택 버튼 업데이트
  private fun updateSelectedButton(newButton: FilterButton, container: LinearLayout) {
    // 이전 선택 버튼 OFF
    (selectedButton as? FilterButton)?.setOffStyle()
    
    // 새로 선택한 버튼 ON
    newButton.setOnStyle()
    selectedButton = newButton
  }
  
  // 필터(구) 리스트
  private fun updateList(filter: String?) {
    placeList.clear()
    if (filter.isNullOrEmpty()) {
      placeList.addAll(originalList)
    } else {
      placeList.addAll(originalList.filter { it.category == filter })
    }
    adapter.notifyDataSetChanged()
    
    // 첫 번째 아이템으로 스크롤
    binding.rvList.scrollToPosition(0)
  }
  
  
  // 검색 리스트
  private fun filterList(query: String?) {
    val searchText = query?.trim()?.lowercase() ?: ""
    placeList.clear()
    
    if (searchText.isEmpty()) {
      placeList.addAll(originalList)
    } else {
      placeList.addAll(
        originalList.filter { place ->
          place.title.lowercase().contains(searchText) ||
            place.menu.lowercase().contains(searchText)
        }
      )
    }
    adapter.notifyDataSetChanged()
  }
  // jin 추가 end
  
}
