package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityListBinding.inflate(layoutInflater) }

    // 공공데이터 포탈 부산맛집 정보 서비스 API key
    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fetchFoodData()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey = serviceKey, numOfRows = 100)
            .enqueue(object : Callback<FoodResponse> {
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        val list = response.body()?.getFoodkr?.item
                        Log.d("ListActivity", "Raw list: $list")

                        if (!list.isNullOrEmpty()) {
                            createButtonsDynamically(list)
                        } else {
                            Toast.makeText(this@ListActivity, "데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("ListActivity", "items 혹은 item이 null이거나 비어있음")
                        }
                    } else {
                        Toast.makeText(this@ListActivity, "서버 응답 실패", Toast.LENGTH_SHORT).show()
                        Log.e("ListActivity", "Response error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("ListActivity", "API 호출 실패", t)
                    Toast.makeText(this@ListActivity, "API 호출 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createButtonsDynamically(items: List<FoodItem>) {
        val container = binding.buttonContainer
        container.removeAllViews()

        items.forEach { foodItem ->
            val button = Button(this).apply {
                text = "UC_SEQ : ${foodItem.UcSeq}" // project2 패키지의 FoodItem 필드명
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(16, 16, 16, 0) }

                setOnClickListener {
                    Toast.makeText(this@ListActivity, "${foodItem.MAIN_TITLE} 클릭됨!", Toast.LENGTH_SHORT).show()
                    navigateToDetail(foodItem)
                }
            }
            container.addView(button)
        }
    }

    private fun navigateToDetail(item: FoodItem) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("title", item.MAIN_TITLE)
            putExtra("addr", item.ADDR)
            putExtra("menu", item.CATE_NM)
            putExtra("imageurl", item.MAIN_IMG)
            putExtra("UcSeq", item.UcSeq)
        }
        startActivity(intent)
    }
}
