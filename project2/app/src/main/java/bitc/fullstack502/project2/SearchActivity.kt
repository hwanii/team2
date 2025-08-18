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
    
    binding.searchEditText.addTextChangedListener { editable ->
      val keyword = editable?.toString() ?: ""
      val selectedCategory = binding.selectSpinner.selectedItem?.toString()
      filterFood(keyword, selectedCategory)
    }
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
      override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
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
}