package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

//    api 키값
    private val servicekey =
        "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    var item: FoodItem? = null


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchFoodData()
    }
    private var foodList:List<FoodItem>? = null
    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey = servicekey)
            .enqueue(object : Callback<FoodResponse> {

//               api 응답 성공
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        Log.d("MainActivity", "데이터 로딩 성공: ${response.body()}")

                        val foodList = response.body()?.getFoodkr?.item

                        if (!foodList.isNullOrEmpty()) {
//                            데이터를 가져오고 값을 넣음
                            this@MainActivity.item = foodList[0]

                            Toast.makeText(this@MainActivity, "${item?.TITLE} 데이터 로딩 완료!", Toast.LENGTH_SHORT).show()

                        } else {
//                            api는 가져왔지만 데이터가 없음
                            Log.d("MainActivity", "데이터가 없습니다.")
                            Toast.makeText(this@MainActivity, "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        //응답 오류
                        Log.e("MainActivity", "서버 응답 오류: ${response.code()}")
                        Toast.makeText(this@MainActivity, "서버에서 응답을 받지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                // api 못가져옴
                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("MainActivity", "API 호출 실패", t)
                    Toast.makeText(this@MainActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        // 버튼 이벤트
        binding.btnDetail.setOnClickListener {
            item?.let {
                val intent = Intent(this, DetailActivity::class.java)
//                api로 가져온 값을 변수에 넣어줌
                intent.putExtra("title", it.TITLE)
                intent.putExtra("addr", it.ADDR)
                intent.putExtra("subaddr",it.SubAddr)
                intent.putExtra("tel", it.TEL)
                intent.putExtra("time", it.Time)
                intent.putExtra("item", it.Item )
                intent.putExtra("imageurl",it.image)
                intent.putExtra("lat",it.Lat ?: 0.0f)
                intent.putExtra("lng",it.Lng ?: 0.0f)
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "데이터가 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)

        }
    }
}