package bitc.fullstack502.project2

import android.content.Intent
import android.location.Location
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


    private var isLike = false

    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        var isLiked = false

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
//         확인용 데이터!!
        val reviews = getMockReviews()

        if (reviews.isNotEmpty()) {
            createReviewsDynamically(reviews)
        }
    }

//        val title = intent.getStringExtra("title")
//        val addr = intent.getStringExtra("addr")
//        val subaddr = intent.getStringExtra("subaddr")
//        val tel = intent.getStringExtra("tel")
//        val time = intent.getStringExtra("time")
//        val item = intent.getStringExtra("item")
//        val imageurl = intent.getStringExtra("imageurl")
//         val GUGUN_NM = intent.getStringExtra("gugum")
//        val lat = intent.getFloatExtra("lat",0.0f).toDouble()
//        val lng = intent.getFloatExtra("lng", 0.0f).toDouble()

    private fun displayCurrentItemDetails(item: FoodItem) {
        binding.txtTitle.text = "⸰ ${item.TITLE }"
        binding.txtAddr.text = "⸰ ${item.ADDR }"
        binding.txtSubAddr.text = "⸰${item.SubAddr}"
        binding.txtTel.text = "⸰${item.TEL}"
        binding.txtTime.text = "⸰${item.Time}"
        binding.txtItem.text = "⸰ ${item.Item}"
        binding.txtAddrcategory.text = "${item.GUGUN_NM} > "

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
        binding.btnLike.setOnClickListener {
            isLike = !isLike

            if(isLike) {
                binding.btnLike.setImageResource(R.drawable.heart_full)
            }else {
                binding.btnLike.setImageResource(R.drawable.heart_none)
            }

        }
    }
    private fun setupRecommendations(currentItem: FoodItem, allItems: List<FoodItem>) {
        // 같은 '구/군'의 다른 맛집들을 필터링하고, 순서를 섞은 후, 최대 3개만 가져옵니다.
        val currentLoc = Location("current")
        val currentLat = currentItem.Lat?.toDouble()
        val currentLng = currentItem.Lng?.toDouble()
        val recommendations: List<FoodItem>

        if (currentLat != null && currentLng != null) {
            currentLoc.latitude = currentLat
            currentLoc.longitude = currentLng

            recommendations = allItems
                .filter { it.UcSeq != currentItem.UcSeq } // 자기 자신 제외
                .mapNotNull { foodItem ->
                    // 추천 후보의 위치 정보가 유효할 때만 거리를 계산
                    val lat = foodItem.Lat?.toDouble()
                    val lng = foodItem.Lng?.toDouble()
                    if (lat != null && lng != null) {
                        val itemLoc = Location("item")
                        itemLoc.latitude = lat
                        itemLoc.longitude = lng
                        val distance = currentLoc.distanceTo(itemLoc)
                        // 거리 제한 조건 (예: 10km 이내)
                        if (distance < 10000) {
                            Pair(foodItem, distance)
                        } else {
                            null
                        }
                    } else {
                        null // 위치 정보 없는 후보는 제외
                    }
                }
                .sortedBy { it.second } // 가까운 순으로 정렬
                .take(3) // 상위 3개 선택
                .map { it.first } // 맛집 정보만 추출

        } else {
            // 3. 현재 맛집 위치 정보가 없으면 '같은 구/군' 랜덤 추천으로 대체
            recommendations = allItems
                .filter { it.GUGUN_NM == currentItem.GUGUN_NM && it.UcSeq != currentItem.UcSeq }
                .shuffled()
                .take(3)
        }

        // 4. 최종 추천 목록을 화면에 표시
        if (recommendations.isNotEmpty()) {
            bindRecommendationData(recommendations[0], allItems, binding.recommend1, binding.recommendImage1, binding.recommendTitle1, binding.recommendGugun1)
        }
        if (recommendations.size > 1) {
            bindRecommendationData(recommendations[1], allItems, binding.recommend2, binding.recommendImage2, binding.recommendTitle2, binding.recommendGugun2)
        }
        if (recommendations.size > 2) {
            bindRecommendationData(recommendations[2], allItems, binding.recommend3, binding.recommendImage3, binding.recommendTitle3, binding.recommendGugun3)
        }
    }

    // 추천 데이터를 각 뷰에 바인딩하는 헬퍼 함수
    private fun bindRecommendationData(
        item: FoodItem,
        allItem: List<FoodItem>,
        container: LinearLayout,
        imageView: ImageView,
        titleView: TextView,
        gugunView: TextView,
    ) {
        container.visibility = View.VISIBLE // 숨겨진 뷰를 보이게 함
        titleView.text = item.TITLE
        gugunView.text = item.GUGUN_NM
        Glide.with(this).load(item.thumb).into(imageView)

        container.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("clicked_item",item)
                putParcelableArrayListExtra("full_list", ArrayList(allItem))
            }
            startActivity(intent)
        }
    }

//    임시 데이터! 여기 밑은 전부 임시로 넣은것!
    private fun getMockReviews(): List<Review> {
        return listOf(
            Review(4.5f, "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.", "2025.08.12"),
            Review(5.0f, "음식이 매우 맛있고 분위기도 좋았습니다. 재방문 의사 100%입니다!", "2025.08.11"),
            Review(3.0f, "조금 아쉬웠지만 그럭저럭 먹을만 했습니다.", "2025.08.10")
        )
    }
    private fun createReviewsDynamically(reviews: List<Review>) {
        // 1단계에서 XML에 추가한 컨테이너를 바인딩으로 가져옴
        val reviewsContainer = binding.reviewsContainer
        reviewsContainer.removeAllViews() // 혹시 모를 기존 뷰 제거

        reviews.forEach { reviewItem ->
            // 리뷰 아이템 하나를 감싸는 전체 틀(수직 LinearLayout)
            val reviewLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(0, 0, 0, 24) // 각 리뷰 사이의 간격
                }
            }

            // 상단 라인(별점, 날짜)을 위한 수평 LinearLayout
            val topRowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val starIcon = ImageView(this).apply {
                setImageResource(R.drawable.star) // 별 아이콘 (ic_star)
                val iconSize = 20
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            }
            val ratingText = TextView(this).apply {
                text = reviewItem.rating.toString()
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(8, 0, 16, 0)
            }
            val dateText = TextView(this).apply {
                text = reviewItem.date
            }

            topRowLayout.addView(starIcon)
            topRowLayout.addView(ratingText)
            topRowLayout.addView(dateText)

            // 리뷰 내용 TextView
            val contentText = TextView(this).apply {
                text = reviewItem.content
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(0, 0, 0, 0) // 각 리뷰 사이의 간격
                }
                // --- 이 부분을 추가하여 배경에 테두리 drawable을 설정합니다. ---
                background = getDrawable(R.drawable.border_bg)
            }

            // 전체 틀에 상단 라인과 리뷰 내용 추가
            reviewLayout.addView(topRowLayout)
            reviewLayout.addView(contentText)

            // 최종적으로 완성된 리뷰 뷰를 화면의 컨테이너에 추가
            reviewsContainer.addView(reviewLayout)
        }
    }
}




