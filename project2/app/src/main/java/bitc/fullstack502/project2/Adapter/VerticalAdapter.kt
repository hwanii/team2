package bitc.fullstack502.project2.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.DetailActivity
import bitc.fullstack502.project2.databinding.ItemVerticalCardBinding
import bitc.fullstack502.project2.model.Item
import com.bumptech.glide.Glide

class VerticalAdapter(private val itemList: List<Item>) :
    RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder>() {

    inner class VerticalViewHolder(val binding: ItemVerticalCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalViewHolder {
        val binding = ItemVerticalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VerticalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerticalViewHolder, position: Int) {
        val item = itemList[position]

        holder.binding.txtTitle.text = item.title
        holder.binding.txtAddress.text = item.address
        holder.binding.ratingBar.rating = item.rating.toFloat()

        Glide.with(holder.itemView.context)
            .load(item.thumbUrl)
            .centerCrop()
            .into(holder.binding.imgPlace)

        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("item", item) // Item이 Parcelable 혹은 Serializable 이어야 합니다
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
