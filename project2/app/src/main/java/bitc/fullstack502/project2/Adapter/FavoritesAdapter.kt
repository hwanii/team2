package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.databinding.ItemFavoriteBinding
import com.bumptech.glide.Glide

class FavoritesAdapter(
    private val items: MutableList<FoodItem>,
    private val likedPlaceCodes: MutableSet<Int>,
    private val currentUserKey: Int
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {
    
    private var listener: ((FoodItem, Int, Boolean) -> Unit)? = null
    private var itemClickListener: ((FoodItem) -> Unit)? = null
    
    // 외부에서 items 접근할 수 있도록 getter 추가
    fun getItems(): List<FoodItem> = items
    
    fun setOnLikeClickListener(listener: (FoodItem, Int, Boolean) -> Unit) {
        this.listener = listener
    }
    
    fun setOnItemClickListener(listener: (FoodItem) -> Unit) {
        this.itemClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoritesViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class FavoritesViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FoodItem) {
            binding.itemTitle.text = item.TITLE
            binding.itemCategoryAddr.text = "${item.GUGUN_NM}"
            
            Glide.with(binding.itemImageView.context)
                .load(item.thumb)
                .into(binding.itemImageView)
            
            // 하트 상태 초기화
            val isLiked = likedPlaceCodes.contains(item.UcSeq)
            binding.likeIcon.setImageResource(
                if (isLiked) R.drawable.heart_full
                else R.drawable.heart_none
            )
            
            // 하트 클릭 처리
            binding.likeIcon.setOnClickListener {
                val currentlyLiked = likedPlaceCodes.contains(item.UcSeq)
                val newLikeState = !currentlyLiked
                listener?.invoke(item, adapterPosition, newLikeState)
            }
            
            binding.root.setOnClickListener {
                itemClickListener?.invoke(item)
            }
        }
    }
    
    fun updateItems(newItems: MutableList<FoodItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
        }
    }
    
    
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