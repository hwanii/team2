package bitc.fullstack502.project2.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.DetailActivity
import bitc.fullstack502.project2.FoodItem
import bitc.fullstack502.project2.R
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class SearchAdapter(private var items: List<FoodItem>) :
  RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
  
  class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.searchTitle)
    val addr: TextView = itemView.findViewById(R.id.searchAddr)
    val image: ShapeableImageView = itemView.findViewById(R.id.searchFoodImage)
    val time: TextView = itemView.findViewById(R.id.searchTime)
  }
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.activity_search_list, parent, false)
    return SearchViewHolder(view)
  }
  
  override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
    val item = items[position]
    holder.title.text = item.TITLE
    holder.addr.text = "${item.GUGUN_NM} · ${item.CATE_NM ?: ""}"
    holder.time.text = item.Time ?: "운영시간 정보 없음"
    
    Glide.with(holder.itemView.context)
      .load(item.thumb)
      .into(holder.image)
    
    holder.itemView.setOnClickListener {
      val context = holder.itemView.context
      val intent = Intent(context, DetailActivity::class.java).apply {
        putExtra("clicked_item", item)
        putParcelableArrayListExtra("full_list", ArrayList(items))
      }
      context.startActivity(intent)
    }
  }
  
  override fun getItemCount(): Int = items.size
  
  fun updateData(newList: List<FoodItem>) {
    items = newList
    notifyDataSetChanged()
  }
}