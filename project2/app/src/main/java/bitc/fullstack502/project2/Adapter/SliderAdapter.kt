package bitc.fullstack502.project2.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.R

class SliderAdapter(
    private val slides: List<SlideItem>,
    private val context: Context
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageSlider)
        val orderText: TextView = view.findViewById(R.id.tvSlideOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val slide = slides[position]
        holder.imageView.setImageResource(slide.imageRes)

        // 클릭 시 URL 이동
        holder.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(slide.url))
            context.startActivity(intent)
        }

        // 오른쪽 위 순서 표시
        holder.orderText.text = "${position + 1}/${slides.size}"
    }

    override fun getItemCount(): Int = slides.size
}
