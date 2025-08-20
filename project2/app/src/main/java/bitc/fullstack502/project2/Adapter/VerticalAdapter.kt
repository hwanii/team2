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

    companion object {
        const val VIEW_TYPE_FOOD = 0
        const val VIEW_TYPE_MORE = 1
    }

    interface ItemClickListener {
        fun onItemClick(item: FoodItem)
        fun onLoadMore()
    }

    private var fullDataList: List<FoodItem> = emptyList()
    private val displayList = mutableListOf<FoodItem>()
    private var showMoreButton = false
    private val pageSize = 5

    inner class FoodViewHolder(val binding: ItemVerticalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class MoreViewHolder(val binding: ItemMoreButtonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (showMoreButton && position == displayList.size) VIEW_TYPE_MORE else VIEW_TYPE_FOOD
    }

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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FoodViewHolder) {
            val item = displayList[position]
            with(holder.binding) {
                itemTitle.text = item.TITLE
                itemCategory.text = cleanMenuText(item.CATE_NM) // 가격 제거된 카테고리
                itemRating.text = "⭐ 0.0"
                itemAddr.text = item.ADDR ?: ""

                Glide.with(itemImageView.context)
                    .load(item.thumb)
                    .centerCrop()
                    .placeholder(android.R.color.transparent)
                    .into(itemImageView)

                root.setOnClickListener { listener.onItemClick(item) }
            }
        } else if (holder is MoreViewHolder) {
            holder.binding.btnMore.setOnClickListener { listener.onLoadMore() }
        }
    }

    override fun getItemCount(): Int = displayList.size + if (showMoreButton) 1 else 0

    fun setFullList(list: List<FoodItem>) {
        fullDataList = list
        displayList.clear()
        displayList.addAll(fullDataList.take(pageSize))
        showMoreButton = fullDataList.size > displayList.size
        notifyDataSetChanged()
    }

    fun addMore() {
        val start = displayList.size
        val end = (start + pageSize).coerceAtMost(fullDataList.size)
        if (start < end) {
            displayList.addAll(fullDataList.subList(start, end))
        }
        showMoreButton = displayList.size < fullDataList.size
        notifyDataSetChanged()
    }

    // DetailActivity에서 쓰던 메뉴 클린 함수
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
