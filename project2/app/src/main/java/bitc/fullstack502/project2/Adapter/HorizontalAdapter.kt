package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.RetrofitClient.reviewApi
import bitc.fullstack502.project2.ReviewResponse
import bitc.fullstack502.project2.databinding.ItemHorizontalCardBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ================================
// HorizontalAdapter.kt
// RecyclerView를 가로 스크롤 형태로 보여주는 어댑터
// 음식(FoodItem) 데이터를 받아서 카드 형태로 바인딩한다
// ================================
class HorizontalAdapter(
    private var itemList: MutableList<FoodItem>, // RecyclerView에 표시할 데이터 목록
    private val listener: ItemClickListener      // 아이템 클릭 이벤트 리스너
) : RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    // ================================
    // 클릭 이벤트 처리를 위한 인터페이스
    // → 어댑터 안에서 처리하지 않고, Activity/Fragment에서 정의할 수 있게 한다
    // ================================
    interface ItemClickListener {
        fun onItemClick(item: FoodItem)
    }

    // ================================
    // ViewHolder : 카드 뷰 1개를 보관하는 클래스
    // binding : item_horizontal_card.xml의 뷰와 연결
    // ================================
    inner class HorizontalViewHolder(val binding: ItemHorizontalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ================================
    // ViewHolder 생성 (XML → 뷰 객체로 변환)
    // ================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemHorizontalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorizontalViewHolder(binding)
    }

    // ================================
    // 데이터 바인딩 : 실제 데이터(FoodItem)를 뷰에 연결하는 부분
    // ================================
    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val item = itemList[position] // 현재 위치의 FoodItem 가져오기

        with(holder.binding) {
            // ------------------------
            // 기본 정보 바인딩
            // ------------------------
            txtTitle.text = item.MAIN_TITLE.ifBlank { item.TITLE } // 메인 제목이 없으면 보조 제목 사용
            txtAddress.text = cleanMenuText(item.ADDR)             // 주소 문자열 정리 후 표시
//            txtCategory.text = cleanMenuText(item.CATE_NM)         // 카테고리 문자열 정리 후 표시
            txtRating.text = "⭐ -"                                // 초기값 (평점이 아직 없을 때)

            // ------------------------
            // 이미지 처리 (Glide 사용)
            // ------------------------
            if (item.thumb.isNullOrBlank()) {
                imgPlace.visibility = View.GONE // 이미지가 없으면 숨김
            } else {
                imgPlace.visibility = View.VISIBLE
                Glide.with(imgPlace.context)
                    .load(item.thumb)           // 이미지 URL
                    .centerCrop()               // 가운데 기준으로 크롭
                    .into(imgPlace)             // ImageView에 로드
            }

            // ------------------------
            // 별점 API 연동
            // ------------------------
            // placeCode가 있어야 해당 장소의 리뷰 조회 가능
            // ⭐ 별점 API 연동
            val placeCode = item.UcSeq   // FoodItem 안의 고유 코드 사용
            reviewApi.getReviews(item.UcSeq)
                .enqueue(object : Callback<List<ReviewResponse>> {
                    override fun onResponse(
                        call: Call<List<ReviewResponse>>,
                        response: Response<List<ReviewResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val reviews = response.body() ?: emptyList()
                            if (reviews.isNotEmpty()) {
                                val avgRating = reviews.map { it.reviewRating }.average().toFloat()
                                txtRating.text = "⭐ %.1f".format(avgRating)
                            } else {
                                txtRating.text = "⭐ 0.0"
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                        txtRating.text = "⭐ 0.0"
                    }
                })

            // ------------------------
            // 카드 클릭 이벤트
            // ------------------------
            root.setOnClickListener {
                listener.onItemClick(item) // 외부에서 정의한 클릭 이벤트 실행
            }
        }
    }

    // ================================
    // 전체 아이템 개수 반환
    // ================================
    override fun getItemCount(): Int = itemList.size

    // ================================
    // 불필요한 텍스트를 제거하는 함수
    // (가격, 단위 등은 제거해서 깔끔하게 보여줌)
    // ================================
    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""

        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")           // 괄호 안 내용 제거
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "") // ₩ + 숫자 제거
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")            // -숫자 제거
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")        // /숫자 제거
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")               // g 단위 제거

        // 여러 메뉴명이 있으면 앞 2개만 표시
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }

    // ================================
    // 새로운 데이터 목록으로 갱신
    // ================================
    fun updateList(newList: List<FoodItem>) {
        itemList.clear()
        // thumb가 없는 아이템은 제외
        val filteredList = newList.filter { !it.thumb.isNullOrBlank() }
        itemList.addAll(filteredList)
        notifyDataSetChanged() // RecyclerView에 데이터 변경 알림
    }
}
