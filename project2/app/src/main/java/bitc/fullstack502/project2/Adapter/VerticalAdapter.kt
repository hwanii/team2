// ================================
// VerticalAdapter.kt
// RecyclerView를 세로 스크롤 형태로 보여주는 어댑터
// 음식(FoodItem) 데이터와 "더보기 버튼"을 함께 처리한다
// ================================
package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.databinding.ItemMoreButtonBinding
import bitc.fullstack502.project2.databinding.ItemVerticalCardBinding
import com.bumptech.glide.Glide

class VerticalAdapter(
    private val listener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입 구분 상수
    companion object {
        const val VIEW_TYPE_FOOD = 0   // 일반 음식 카드
        const val VIEW_TYPE_MORE = 1   // 더보기 버튼
    }

    // 외부에서 클릭 이벤트 처리할 수 있도록 인터페이스 제공
    interface ItemClickListener {
        fun onItemClick(item: FoodItem) // 카드 클릭 시
        fun onLoadMore()                // "더보기" 버튼 클릭 시
    }

    // 전체 데이터 (서버에서 받은 전체 리스트)
    private var fullDataList: List<FoodItem> = emptyList()
    // 현재 화면에 보여주는 데이터 (일부만 표시)
    private val displayList = mutableListOf<FoodItem>()
    // "더보기" 버튼 표시 여부
    private var showMoreButton = false
    // 한 번에 보여줄 아이템 개수
    private val pageSize = 5

    // 음식 카드 뷰홀더
    inner class FoodViewHolder(val binding: ItemVerticalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // "더보기" 버튼 뷰홀더
    inner class MoreViewHolder(val binding: ItemMoreButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    // 위치에 따라 어떤 뷰 타입을 쓸지 결정
    override fun getItemViewType(position: Int): Int {
        return if (showMoreButton && position == displayList.size) VIEW_TYPE_MORE else VIEW_TYPE_FOOD
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOD) {
            // 음식 카드 레이아웃
            val binding = ItemVerticalCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FoodViewHolder(binding)
        } else {
            // "더보기" 버튼 레이아웃
            val binding = ItemMoreButtonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MoreViewHolder(binding)
        }
    }

    // 데이터 바인딩 처리
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FoodViewHolder) {
            // 음식 카드인 경우
            val item = displayList[position]
            with(holder.binding) {
                itemTitle.text = item.TITLE
                itemCategory.text = cleanMenuText(item.CATE_NM) // 불필요한 텍스트 정리된 카테고리
                itemRating.text = "⭐ 0.0" // 임시 별점
                itemAddr.text = item.ADDR ?: ""

                // Glide로 이미지 불러오기
                Glide.with(itemImageView.context)
                    .load(item.thumb)
                    .centerCrop()
                    .placeholder(android.R.color.transparent)
                    .into(itemImageView)

                // 카드 클릭 이벤트 전달
                root.setOnClickListener { listener.onItemClick(item) }
            }
        } else if (holder is MoreViewHolder) {
            // "더보기" 버튼인 경우 → 클릭 이벤트 전달
            holder.binding.btnMore.setOnClickListener { listener.onLoadMore() }
        }
    }

    // 보여줄 아이템 개수 (더보기 버튼 포함 여부에 따라 달라짐)
    override fun getItemCount(): Int = displayList.size + if (showMoreButton) 1 else 0

    // 전체 데이터 설정 (최초 로딩 시 호출)
    fun setFullList(list: List<FoodItem>) {
        fullDataList = list
        displayList.clear()
        // 처음에는 pageSize 만큼만 표시
        displayList.addAll(fullDataList.take(pageSize))
        // 남은 데이터가 있다면 "더보기" 버튼 활성화
        showMoreButton = fullDataList.size > displayList.size
        notifyDataSetChanged()
    }

    // "더보기" 버튼 눌렀을 때 → 데이터 추가 로드
    fun addMore() {
        val start = displayList.size
        val end = (start + pageSize).coerceAtMost(fullDataList.size)
        if (start < end) {
            displayList.addAll(fullDataList.subList(start, end))
        }
        // 아직 남은 데이터가 있으면 "더보기" 유지
        showMoreButton = displayList.size < fullDataList.size
        notifyDataSetChanged()
    }

    // 텍스트 정리 (가격, 단위 등 제거)
    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""
        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")          // 괄호 내용 제거
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "") // 가격 제거
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")           // -숫자 제거
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")       // /숫자 제거
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")              // g 단위 제거
        cleanedText = cleanedText.replace("\n", ", ")                      // 줄바꿈 → 콤마
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }
}
