// ================================
// HorizontalAdapter.kt
// RecyclerView를 가로 스크롤 형태로 보여주는 어댑터
// 음식(FoodItem) 데이터를 받아서 카드 형태로 바인딩한다
// ================================
package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.databinding.ItemHorizontalCardBinding
import com.bumptech.glide.Glide

// RecyclerView.Adapter를 상속받아 어댑터 구현
// itemList : 화면에 표시할 데이터 목록
// listener : 아이템 클릭 이벤트를 외부에 전달하기 위한 인터페이스
class HorizontalAdapter(
    private var itemList: MutableList<FoodItem>,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    // 클릭 이벤트 처리를 위한 인터페이스
    // → 어댑터 안에서 처리하지 않고, Activity/Fragment에서 동작을 정의할 수 있게 함
    interface ItemClickListener {
        fun onItemClick(item: FoodItem)
    }

    // 뷰홀더 : 화면에 표시할 각 아이템(카드)의 뷰를 들고 있음
    inner class HorizontalViewHolder(val binding: ItemHorizontalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // 레이아웃을 실제 뷰 객체로 생성하는 부분 (ViewHolder 생성)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemHorizontalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorizontalViewHolder(binding)
    }

    // 실제 데이터(itemList의 내용)를 뷰홀더에 바인딩
    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val item = itemList[position] // 현재 위치의 아이템 가져오기
        with(holder.binding) {
            // 메인 제목이 비어있으면 보조 제목 사용
            txtTitle.text = item.MAIN_TITLE.ifBlank { item.TITLE }
            // 주소와 카테고리는 불필요한 문자를 제거(cleanMenuText) 후 표시
            txtAddress.text = cleanMenuText(item.ADDR)
            txtCategory.text = cleanMenuText(item.CATE_NM)
            // 기본 별점 (추후 실제 리뷰 점수와 연동 가능)
            txtRating.text = "⭐ 0.0"

            // 이미지가 없는 경우 → 이미지 뷰를 숨김
            if (item.thumb.isNullOrBlank()) {
                imgPlace.visibility = View.GONE
            } else {
                // 이미지가 있는 경우 → Glide 라이브러리로 불러와 표시
                imgPlace.visibility = View.VISIBLE
                Glide.with(imgPlace.context)
                    .load(item.thumb)
                    .centerCrop()
                    .into(imgPlace)
            }

            // 카드 전체를 클릭했을 때 → 외부로 클릭 이벤트 전달
            root.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    // 표시할 아이템 개수 반환
    override fun getItemCount(): Int = itemList.size

    // 불필요한 텍스트(가격, 단위 등)를 정리해주는 함수
    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""

        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "") // 괄호 안의 내용 제거
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "") // 가격 제거
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "") // -숫자 형태 제거
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "") // /숫자 형태 제거
        cleanedText = cleanedText.replace(Regex("\\d+g"), "") // g 단위 제거

        // 메뉴 이름 여러 개가 있으면 앞의 2개만 표시
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }

    // 외부에서 새로운 데이터 목록을 전달받아 갱신하는 함수
    fun updateList(newList: List<FoodItem>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged() // RecyclerView에 데이터가 바뀌었음을 알림
    }
}
