package bitc.fullstack502.project2

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
import bitc.fullstack502.project2.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
  
  private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
  
  private var fullFoodList: List<FoodItem> = emptyList() // 전체 데이터 저장
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(binding.root)
    
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    
    // API 호출 후 Spinner 세팅
    loadFoodCategories()
    
    // 검색창 입력 이벤트 처리 (Enter 키 입력 시)
    binding.searchEditText.setOnEditorActionListener { _, _, _ ->
      val keyword = binding.searchEditText.text.toString()
      filterFood(keyword, binding.selectSpinner.selectedItem?.toString())
      true
    }
  }
  
  private fun loadFoodCategories() {
    val serviceKey = "2i6hBH%2Fw7lNbUMoXiq1NuV%2FysUs%2BflIBzypTyxsWYaEgfFZ1xUHbxXuNdAlrZ14DPqS%2F43LoetOpnXDWMz4JBg%3D%3D"
    
    RetrofitClient.api.getFoodList(serviceKey = serviceKey).enqueue(object : Callback<FoodResponse> {
      override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
        if (response.isSuccessful) {
          val foodList = response.body()?.getFoodkr?.item ?: emptyList()
          fullFoodList = foodList
          setupSpinner(foodList)
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
    // 구군만 추출 후 중복 제거
    val categories = mutableListOf("전체")
    categories.addAll(foodList.map { it.GUGUN_NM }.distinct())
    
    val adapter = ArrayAdapter(
      this,
      android.R.layout.simple_spinner_item,
      categories
    ).apply {
      setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    
    binding.selectSpinner.adapter = adapter
    
    binding.selectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(
        parent: AdapterView<*>, view: View?, position: Int, id: Long
      ) {
        val selectedCategory = categories[position]
        filterFood(binding.searchEditText.text.toString(), selectedCategory)
      }
      
      override fun onNothingSelected(parent: AdapterView<*>) {}
    }
  }
  
  private fun filterFood(keyword: String?, category: String?) {
    val filtered = fullFoodList.filter { item ->
      val matchesKeyword = keyword.isNullOrBlank() ||
        item.TITLE.contains(keyword, ignoreCase = true) ||
        item.GUGUN_NM.contains(keyword, ignoreCase = true) ||
        (item.MAIN_TITLE?.contains(keyword, ignoreCase = true) ?: false) // null 안전하게 처리
      
      val matchesCategory = category.isNullOrBlank() || category == "전체" || item.GUGUN_NM == category
      
      matchesKeyword && matchesCategory
    }
    
    Log.d("SearchActivity", "필터 결과:\n" + filtered.joinToString("\n") {
      "TITLE: ${it.TITLE}, GUGUN_NM: ${it.GUGUN_NM}, MAIN_TITLE: ${it.MAIN_TITLE ?: ""}"
    })
    Toast.makeText(this, "${filtered.size}개 결과", Toast.LENGTH_SHORT).show()
  }
}