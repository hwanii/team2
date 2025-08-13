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
 * 세로 리스트 어댑터
 * @param itemList - Item 데이터 리스트
 */
class VerticalAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder>() {

    inner class VerticalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val rating: RatingBar = view.findViewById(R.id.ratingBar)
        val address: TextView = view.findViewById(R.id.txtAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vertical, parent, false)
        return VerticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerticalViewHolder, position: Int) {
        val item = itemList[position]
        holder.title.text = item.title
        holder.rating.rating = item.rating.toFloat()
        holder.address.text = item.address
    }

    override fun getItemCount(): Int = itemList.size
}