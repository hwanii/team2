package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.DetailActivity
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.databinding.ItemHorizontalCardBinding
import com.bumptech.glide.Glide

// FoodItem 전용 HorizontalAdapter
class HorizontalAdapter(
    private val itemList: List<FoodItem>,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(item: FoodItem)
    }

    inner class HorizontalViewHolder(val binding: ItemHorizontalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemHorizontalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val item = itemList[position]

        with(holder.binding) {
            txtTitle.text = item.TITLE
            txtAddress.text = cleanMenuText(item.ADDR)
            txtCategory.text = cleanMenuText(item.CATE_NM)
            txtRating.text = "⭐ 0.0" // 평점 없으면 0.0 처리

            Glide.with(imgPlace.context)
                .load(item.thumb)
                .centerCrop()
                .into(imgPlace)

            // 클릭 이벤트
            root.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""
        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }
}
