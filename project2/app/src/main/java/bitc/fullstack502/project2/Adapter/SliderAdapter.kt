package bitc.fullstack502.project2.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.SearchActivity

class SliderAdapter(
    private val slides: List<SlideItem>,
    private val context: Context
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageSlider)
        val orderText: TextView = view.findViewById(R.id.tvSlideOrder)
        val sliderLabel: TextView = view.findViewById(R.id.sliderLabel) // 이미지 위 텍스트
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val slide = slides[position]
        holder.imageView.setImageResource(slide.imageRes)

        // 오른쪽 위 순서 표시
        holder.orderText.text = "${position + 1}/${slides.size}"

        // 이미지 위 레이블 텍스트
        holder.sliderLabel.text = when(position) {
            0 -> "밥 검색"
            1 -> "조개 검색"
            2 -> "찜 검색"
            3 -> "고기 검색"
            else -> ""
        }

        // 클릭 시 SearchActivity로 이동
        holder.imageView.setOnClickListener {
            val keyword = when(position) {
                0 -> "밥"
                1 -> "조개"
                2 -> "찜"
                3 -> "고기"
                else -> ""
            }

            if (keyword.isNotEmpty()) {
                val intent = Intent(context, SearchActivity::class.java).apply {
                    putExtra("search_keyword", keyword)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = slides.size
}
