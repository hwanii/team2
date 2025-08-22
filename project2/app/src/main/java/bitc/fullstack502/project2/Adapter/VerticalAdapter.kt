// ================================
// VerticalAdapter.kt
// RecyclerView를 세로 스크롤 형태로 보여주는 어댑터
// 음식(FoodItem) 데이터와 "더보기 버튼"을 함께 처리하고,
// 리뷰 API를 통해 별점을 가져와 표시함
// ================================
package bitc.fullstack502.project2.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.ReviewApiService
import bitc.fullstack502.project2.ReviewResponse
import bitc.fullstack502.project2.databinding.ItemMoreButtonBinding
import bitc.fullstack502.project2.databinding.ItemVerticalCardBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerticalAdapter(
    private val listener: ItemClickListener,
    private val reviewApi: ReviewApiService // Retrofit으로 생성한 ReviewApiService 주입
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ================================
    // 뷰 타입 상수
    // VIEW_TYPE_FOOD : 음식 카드
    // VIEW_TYPE_MORE : "더보기" 버튼
    // ================================
    companion object {
        const val VIEW_TYPE_FOOD = 0
        const val VIEW_TYPE_MORE = 1
    }

    // ================================
    // 클릭 이벤트 인터페이스
    // ================================
    interface ItemClickListener {
        fun onItemClick(item: FoodItem) // 카드 클릭
        fun onLoadMore()                // "더보기" 버튼 클릭
    }

    // ================================
    // 데이터 관리
    // fullDataList : 서버에서 받은 전체 데이터
    // displayList : 화면에 보여주는 데이터 (일부만 표시)
    // showMoreButton : 더보기 버튼 표시 여부
    // pageSize : 한 번에 보여줄 아이템 수
    // ================================
    private var fullDataList: List<FoodItem> = emptyList()
    private val displayList = mutableListOf<FoodItem>()
    private var showMoreButton = false
    private val pageSize = 5

    // ================================
    // 뷰홀더 정의
    // FoodViewHolder : 음식 카드
    // MoreViewHolder : "더보기" 버튼
    // ================================
    inner class FoodViewHolder(val binding: ItemVerticalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class MoreViewHolder(val binding: ItemMoreButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ================================
    // 뷰 타입 결정
    // 마지막 위치이면서 showMoreButton true이면 더보기 버튼
    // 나머지는 음식 카드
    // ================================
    override fun getItemViewType(position: Int): Int {
        return if (showMoreButton && position == displayList.size) VIEW_TYPE_MORE else VIEW_TYPE_FOOD
    }

    // ================================
    // 뷰홀더 생성
    // ================================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOD) {
            val binding = ItemVerticalCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodViewHolder(binding)
        } else {
            val binding = ItemMoreButtonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MoreViewHolder(binding)
        }
    }

    // ================================
    // 데이터 바인딩
    // ================================
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FoodViewHolder) {
            val item = displayList[position]
            with(holder.binding) {
                // 제목, 카테고리, 주소 바인딩
                itemTitle.text = item.TITLE
                itemCategory.text = cleanMenuText(item.CATE_NM)
                itemAddr.text = item.ADDR ?: ""

                // Glide로 이미지 불러오기
                Glide.with(itemImageView.context)
                    .load(item.thumb)
                    .centerCrop()
                    .placeholder(android.R.color.transparent)
                    .into(itemImageView)

                // -------------------------
                // 리뷰 API 호출 → 평균 별점 계산
                // -------------------------
                reviewApi.getReviews(item.UcSeq)
                    .enqueue(object : Callback<List<ReviewResponse>> {
                        override fun onResponse(
                            call: Call<List<ReviewResponse>>,
                            response: Response<List<ReviewResponse>>
                        ) {
                            val reviews = response.body() ?: emptyList()
                            val avgRating = if (reviews.isNotEmpty()) {
                                reviews.map { it.reviewRating }.average().toFloat()
                            } else 0.0f

                            // 별점 TextView 업데이트
                            itemRating.text = "⭐ %.1f".format(avgRating)

                            // 디버그 로그
                            Log.d("VerticalAdapter", "placeCode=${item.UcSeq}, avgRating=$avgRating")
                        }

                        override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                            // 실패 시 기본 별점 표시
                            itemRating.text = "⭐ 0.0"
                            Log.e("VerticalAdapter", "placeCode=${item.UcSeq} 리뷰 로딩 실패: ${t.message}")
                        }
                    })

                // 카드 클릭 이벤트 전달
                root.setOnClickListener { listener.onItemClick(item) }

            }
        } else if (holder is MoreViewHolder) {
            // "더보기" 버튼 클릭 이벤트
            holder.binding.btnMore.setOnClickListener { listener.onLoadMore() }
        }
    }

    // ================================
    // 총 아이템 개수 반환
    // displayList.size + 더보기 버튼 여부
    // ================================
    override fun getItemCount(): Int = displayList.size + if (showMoreButton) 1 else 0

    // ================================
    // 전체 데이터 설정 (초기 로딩)
    // ================================
    fun setFullList(list: List<FoodItem>) {
        fullDataList = list
        displayList.clear()
        displayList.addAll(fullDataList.take(pageSize))
        showMoreButton = fullDataList.size > displayList.size
        notifyDataSetChanged()
    }

    // ================================
    // "더보기" 버튼 클릭 시 추가 데이터 로드
    // ================================
    fun addMore() {
        val start = displayList.size
        val end = (start + pageSize).coerceAtMost(fullDataList.size)
        if (start < end) displayList.addAll(fullDataList.subList(start, end))
        showMoreButton = displayList.size < fullDataList.size
        notifyDataSetChanged()
    }

    // ================================
    // 텍스트 정리 함수
    // 가격, 단위, 괄호 내용 제거 후 앞 2개만 표시
    // ================================
    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""
        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")
        cleanedText = cleanedText.replace("\n", ", ")
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }
}
