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
    //    공공데이터 포탈 부산맛집 정보 서비스 api key값
    private val servicekey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

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
//        부산맛집 자료 가져오기 = numOfRows = 100 -> 총 100개 가져옴 , 페이지 기능 만들려면 뒤에 pageNo = 변수값
        RetrofitClient.api.getFoodList(serviceKey = servicekey, numOfRows = 100) // numOfRows = 100 , pageNo = num
            .enqueue(object : Callback<FoodResponse> {
                override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                    if (response.isSuccessful) {
                        val foodList = response.body()?.getFoodkr?.item
                        if (!foodList.isNullOrEmpty()) {
                            // 데이터를 성공적으로 받을때 버튼 생성
                            createButtonsDynamically(foodList)
                        }
                    }
                }
                //                 데이터 받기 실패시 호출
                override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                    Log.e("ListActivity", "API 호출 실패", t)
                }
            })
    }

    //    버튼 생성
    private fun createButtonsDynamically(items: List<FoodItem>) {
        val container = binding.buttonContainer
//        container.orientation = LinearLayout.VERTICAL  EX) Linearlayout.orientation 값 추가 방법
        container.removeAllViews()

        items.forEach { foodItem ->
            val button = Button(this).apply {
//                버튼 text 설정
                text = "UC_SEQ : ${foodItem.UcSeq}"
//                Layout 기능 만드는 것
                layoutParams = LinearLayout.LayoutParams (
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(16,16,16,0)
                }
//                 윗 부분 작성시 android:layout_width="match_parent"
//                              android:layout_width="match_parent"
//                              android:layout_marginLeft , Right , Bottom , Top ="16dp"
//                 의 레이아웃이 설정됨 ,

//                버튼 누를시 확인용 / 지워도 문제없습니다
                setOnClickListener {
                    Log.d("ListActivity", "Button clicked for UC_SEQ: ${foodItem.UcSeq}")
                    Toast.makeText(this@ListActivity, "${foodItem.TITLE} 클릭됨!", Toast.LENGTH_SHORT).show()
                    navigateToDatail(foodItem)

                }
            }
//            화면에 출력하는 소스
            container.addView(button)
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
}
