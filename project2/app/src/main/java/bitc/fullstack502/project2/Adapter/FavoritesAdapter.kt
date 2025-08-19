package bitc.fullstack502.project2.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.DetailActivity
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.databinding.ItemFavoritBinding // item_favorit.xml에 맞춰 자동 생성된 바인딩 클래스
import com.bumptech.glide.Glide

class FavoritesAdapter(private var items: MutableList<FoodItem>, private val allItems: List<FoodItem>) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {




    // 하트 아이콘 클릭 이벤트를 처리하기 위한 리스너 인터페이스
    interface OnLikeClickListener {
        fun onUnlikeClick(item: FoodItem, position: Int)
    }

    private var likeClickListener: OnLikeClickListener? = null

    fun setOnLikeClickListener(listener: OnLikeClickListener) {
        this.likeClickListener = listener
    }

    // ViewHolder: 각 아이템 뷰의 구성요소를 보관하는 객체
    inner class ViewHolder(private val binding: ItemFavoritBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodItem) {
            // 1. 뷰에 데이터 채우기
            binding.itemTitle.text = item.TITLE
            val cleanedMenu = cleanMenuText(item.CATE_NM)
            binding.itemDescription.text = cleanedMenu.ifBlank { "대표 메뉴 정보 없음" }
            binding.itemRating.text = "4.5" // 평점 데이터가 없으므로 임시 값 사용
            binding.itemCategoryAddr.text = "${item.GUGUN_NM}"

            Glide.with(itemView.context)
                .load(item.thumb) // 썸네일 이미지 로드
                .into(binding.itemImageView)

            // 2. 아이템 전체 클릭 리스너 (상세 페이지로 이동)
            itemView.setOnClickListener {
                Log.d("DEBUG_FAVORITES", "아이템 클릭됨! 제목: ${item.TITLE}, ID: ${item.UcSeq}")
                val intent = Intent(itemView.context, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)
                    // DetailActivity가 전체 리스트를 필요로 할 경우에 대비
                    putParcelableArrayListExtra("full_list", ArrayList(allItems))
                }
                itemView.context.startActivity(intent)
            }

            // 3. 하트 아이콘 클릭 리스너 (즐겨찾기 해제)
            binding.likeIcon.setOnClickListener {
                likeClickListener?.onUnlikeClick(item, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFavoritBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // 즐겨찾기에서 아이템을 제거하고 화면을 갱신하는 함수
    fun removeItem(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }
    private fun cleanMenuText(menu: String?):String {
        if(menu.isNullOrBlank()) {
            return ""
        }
        var cleanedText = menu

        cleanedText = cleanedText.replace(Regex("\\(.*\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦Ww원][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")
        cleanedText = cleanedText.replace(Regex("\n"), "")

        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }

        val limitedMenuItems = menuItems.take(2)

        return limitedMenuItems.joinToString(", ")

    }
}