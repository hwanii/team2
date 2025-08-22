package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import bitc.fullstack502.project2.Adapter.SearchAdapter
import bitc.fullstack502.project2.databinding.ActivitySearchBinding
import androidx.core.widget.addTextChangedListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

  private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }

  private var fullFoodList: List<FoodItem> = emptyList()
  private lateinit var searchAdapter: SearchAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    binding.searchView.layoutManager = LinearLayoutManager(this)
    searchAdapter = SearchAdapter(emptyList())
    binding.searchView.adapter = searchAdapter

    loadFoodCategories()

    // 🔹 추가: 슬라이드 클릭에서 전달된 키워드 처리
    val keywordFromSlide = intent.getStringExtra("search_keyword")
    if (!keywordFromSlide.isNullOrBlank()) {
      binding.searchEditText.setText(keywordFromSlide) // EditText에 표시
      filterFood(keywordFromSlide, binding.selectSpinner.selectedItem?.toString())
    }

    binding.searchEditText.addTextChangedListener { editable ->
      val keyword = editable?.toString() ?: ""
      val selectedCategory = binding.selectSpinner.selectedItem?.toString()
      filterFood(keyword, selectedCategory)
    }
    
    setupBottomNavigation()
  }

  private fun loadFoodCategories() {
    val serviceKey = "2i6hBH%2Fw7lNbUMoXiq1NuV%2FysUs%2BflIBzypTyxsWYaEgfFZ1xUHbxXuNdAlrZ14DPqS%2F43LoetOpnXDWMz4JBg%3D%3D"
    val numRows = 500
    val pageNo = 1

    RetrofitClient.api.getFoodList(
      serviceKey = serviceKey,
      pageNo = pageNo,
      numOfRows = numRows
    ).enqueue(object : Callback<FoodResponse> {
      override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
        if (response.isSuccessful) {
          val foodList = response.body()?.getFoodkr?.item ?: emptyList()
          fullFoodList = foodList
          setupSpinner(foodList)
          searchAdapter.updateData(foodList)
        } else {
          Toast.makeText(this@SearchActivity, "API 호출 실패", Toast.LENGTH_SHORT).show()
        }
      }

      override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this@SearchActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
      }
    })
  }

  private fun setupSpinner(foodList: List<FoodItem>) {
    val categories = mutableListOf("전체")
    categories.addAll(foodList.map { it.GUGUN_NM }.distinct())

    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
      setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    binding.selectSpinner.adapter = adapter

    binding.selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selectedCategory = binding.selectSpinner.selectedItem?.toString()
        val keyword = binding.searchEditText.text.toString()
        filterFood(keyword, selectedCategory)
      }

      override fun onNothingSelected(parent: AdapterView<*>) {}
    }
  }

  private fun filterFood(keyword: String?, category: String?) {
    val filtered = fullFoodList.filter { item ->
      val matchesKeyword = keyword.isNullOrBlank() ||
              item.TITLE.contains(keyword, ignoreCase = true) ||
              item.MAIN_TITLE.contains(keyword, ignoreCase = true) ||
              (item.CATE_NM?.contains(keyword, ignoreCase = true) ?: false)

      val matchesCategory = category.isNullOrBlank() || category == "전체" || item.GUGUN_NM == category

      matchesKeyword && matchesCategory
    }

    searchAdapter.updateData(filtered)
    Toast.makeText(this, "${filtered.size}개 결과", Toast.LENGTH_SHORT).show()
    Log.d("SearchActivity", "필터 결과: ${filtered.map { it.TITLE }}")
  }
  
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
          if (!fullFoodList.isNullOrEmpty()) {
            val intent = Intent(this, FavoritesActivity::class.java).apply {
              putParcelableArrayListExtra(
                "full_list",
                ArrayList(fullFoodList) // 전체 음식 리스트 전달
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
