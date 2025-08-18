package bitc.fullstack502.project2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.databinding.ItemHorizontalCardBinding
import bitc.fullstack502.project2.model.Item
import com.bumptech.glide.Glide

class HorizontalAdapter(
    private val items: List<Item>,
    private val onItemClick: ((Item) -> Unit)? = null
) : RecyclerView.Adapter<HorizontalAdapter.HorizontalViewHolder>() {

    inner class HorizontalViewHolder(
        val binding: ItemHorizontalCardBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemHorizontalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            txtTitle.text = item.title
            txtCategory.text = item.category
            ratingBar.rating = item.rating.toFloat()

            // Glide로 이미지 로딩
            Glide.with(imgPlace.context)
                .load(item.thumbUrl)
                .centerCrop()
                .into(imgPlace)

            root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
