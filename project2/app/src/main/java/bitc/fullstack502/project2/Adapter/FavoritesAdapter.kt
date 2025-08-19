package bitc.fullstack502.project2.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.DetailActivity
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.databinding.ItemFavoritBinding
import com.bumptech.glide.Glide

class FavoritesAdapter(
    private var items: MutableList<FoodItem>,
    private val allItems: List<FoodItem>,
    private val likedPlaceCodes: MutableSet<Int>
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {
    
    interface OnLikeClickListener {
        fun onLikeToggle(item: FoodItem, position: Int, isLiked: Boolean)
    }
    
    private var likeClickListener: OnLikeClickListener? = null
    fun setOnLikeClickListener(listener: OnLikeClickListener) { this.likeClickListener = listener }
    
    inner class ViewHolder(private val binding: ItemFavoritBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FoodItem) {
            binding.itemTitle.text = item.TITLE
            binding.itemDescription.text = item.CATE_NM ?: "대표 메뉴 정보 없음"
            binding.itemCategoryAddr.text = item.GUGUN_NM
            binding.itemRating.text = "4.5"
            
            Glide.with(itemView.context).load(item.thumb).into(binding.itemImageView)
            
            // 상세 페이지 이동
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java).apply {
                    putExtra("clicked_item", item)
                    putParcelableArrayListExtra("full_list", ArrayList(allItems))
                }
                itemView.context.startActivity(intent)
            }
            
            // 하트 클릭 (즐겨찾기 토글)
            binding.likeIcon.setOnClickListener {
                val isLiked = likedPlaceCodes.contains(item.UcSeq)
                if (isLiked) {
                    likedPlaceCodes.remove(item.UcSeq)
                    binding.likeIcon.setImageResource(android.R.drawable.btn_star_big_off)
                } else {
                    likedPlaceCodes.add(item.UcSeq)
                    binding.likeIcon.setImageResource(android.R.drawable.btn_star_big_on)
                }
                likeClickListener?.onLikeToggle(item, adapterPosition, !isLiked)
            }
            
            // 하트 초기 상태
            val initialLiked = likedPlaceCodes.contains(item.UcSeq)
            binding.likeIcon.setImageResource(
                if (initialLiked) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoritBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    
    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""
        
        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦Ww원][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")
        cleanedText = cleanedText.replace(Regex("\n"), "")
        
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        return menuItems.take(2).joinToString(", ")
    }
}