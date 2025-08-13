package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.model.Item

/**
 * 가로 스크롤용 어댑터
 * @param itemList - Item 데이터 리스트
 */
class HorizontalAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    inner class HorizontalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val rating: RatingBar = view.findViewById(R.id.ratingBar)
        val category: TextView = view.findViewById(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horizontal, parent, false)
        return HorizontalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.rating.rating = item.rating.toFloat()
        holder.category.text = item.category
    }

    override fun getItemCount(): Int = itemList.size
}