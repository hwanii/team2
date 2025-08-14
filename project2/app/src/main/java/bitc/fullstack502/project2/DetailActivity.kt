package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import bitc.fullstack502.project2.databinding.ActivityDetailBinding
import bitc.fullstack502.project2.databinding.ActivityMapBinding

class DetailActivity : AppCompatActivity() {


    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val currentItem: FoodItem? = intent.getParcelableExtra("clicked_item")
        val allItems: ArrayList<FoodItem>? = intent.getParcelableArrayListExtra("full_list")
        if (currentItem == null) {
            finish()
            return
        }
        displayCurrentItemDetails(currentItem)


        if(!allItems.isNullOrEmpty()) {
            setupRecommendations(currentItem, allItems)
        }
    }

//        val title = intent.getStringExtra("title")
//        val addr = intent.getStringExtra("addr")
//        val subaddr = intent.getStringExtra("subaddr")
//        val tel = intent.getStringExtra("tel")
//        val time = intent.getStringExtra("time")
//        val item = intent.getStringExtra("item")
//        val imageurl = intent.getStringExtra("imageurl")
//        val lat = intent.getFloatExtra("lat",0.0f).toDouble()
//        val lng = intent.getFloatExtra("lng", 0.0f).toDouble()

    private fun displayCurrentItemDetails(item: FoodItem) {
        binding.txtTitle.text = item.TITLE
        binding.txtAddr.text = item.ADDR
        binding.txtSubAddr.text = item.SubAddr
        binding.txtTel.text = item.TEL
        binding.txtTime.text = item.Time
        binding.txtItem.text = item.Item

        Glide.with(this)
            .load(item.image)
            .into(binding.txtImage)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("lat", item.Lat)
                putExtra("lng", item.Lng)
                putExtra("title", item.TITLE)
                putExtra("addr", item.ADDR)

            }
            startActivity(intent)
        }
    }
    private fun setupRecommendations(currentItem: FoodItem, allItems: List<FoodItem>) {
        // 같은 '구/군'의 다른 맛집들을 필터링하고, 순서를 섞은 후, 최대 3개만 가져옵니다.
        val recommendations = allItems
            .filter { it.GUGUN_NM == currentItem.GUGUN_NM && it.UcSeq != currentItem.UcSeq }
            .shuffled()
            .take(3)
            .toMutableList()

        if (recommendations.isNotEmpty()) { // recommendations.size > 0 과 동일
            bindRecommendationData(recommendations[0], binding.recommend1, binding.recommendImage1, binding.recommendTitle1, binding.recommendGugun1, binding.recommendAddr1)
        }

        // 두 번째 추천 아이템이 있는지 확인하고 표시
        if (recommendations.size > 1) {
            bindRecommendationData(recommendations[1], binding.recommend2, binding.recommendImage2, binding.recommendTitle2, binding.recommendGugun2, binding.recommendAddr2)
        }

        // 세 번째 추천 아이템이 있는지 확인하고 표시
        if (recommendations.size > 2) {
            bindRecommendationData(recommendations[2], binding.recommend3, binding.recommendImage3, binding.recommendTitle3, binding.recommendGugun3, binding.recommendAddr3)
        }
    }

    // 추천 데이터를 각 뷰에 바인딩하는 헬퍼 함수
    private fun bindRecommendationData(
        item: FoodItem,
        container: LinearLayout,
        imageView: ImageView,
        titleView: TextView,
        gugunView: TextView,
        addrView: TextView
    ) {
        container.visibility = View.VISIBLE // 숨겨진 뷰를 보이게 함
        titleView.text = item.TITLE
        gugunView.text = item.GUGUN_NM
        addrView.text = item.ADDR
        Glide.with(this).load(item.image).into(imageView)
    }
}
