package bitc.fullstack502.project2

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.data.Place
import bitc.fullstack502.project2.databinding.ItemFoodBinding
import com.bumptech.glide.Glide

class PlaceAdapter(
    private val placeList: List<Place>,
    private val itemClickListener: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.tvTitle.text = place.title

            val rating = place.rating.toString()
            val category = place.category
            val menu = place.menu.replace("\n", " ")

            val text = "  $rating · $category · $menu" // 공백 2칸은 별 아이콘 자리
            val spannable = SpannableString(text)

            // 별 아이콘
            val starDrawable = binding.root.context.getDrawable(R.drawable.ic_star)
            starDrawable?.setBounds(0, 0, starDrawable.intrinsicWidth, starDrawable.intrinsicHeight)
            val imageSpan = CenterAlignImageSpan(starDrawable!!)
            spannable.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            // rating 부분만 Bold 처리
            val start = 2
            val end = start + rating.length
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            binding.tvAddr.text = spannable

            // 운영 시간
            val rawTime = place.time ?: ""

            val timeText = "  $rawTime" // 공백 2칸은 시계 아이콘 자리
            val timeSpannable = SpannableString(timeText)

            // 시계 아이콘
            val clockDrawable = binding.root.context.getDrawable(R.drawable.ic_time)
            clockDrawable?.setBounds(0, 0, clockDrawable.intrinsicWidth, clockDrawable.intrinsicHeight)
            timeSpannable.setSpan(CenterAlignImageSpan(clockDrawable!!), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            // 줄바꿈 후 들여쓰기
            val marginPx = 40
            timeSpannable.setSpan(
                LeadingMarginSpan.Standard(marginPx, 0),
                1, // 시계 아이콘 뒤부터 적용
                timeSpannable.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.tvTime.text = timeSpannable

            Glide.with(binding.root.context)
                .load(place.imageUrl)
                .into(binding.ivFoodImage)

            binding.root.setOnClickListener {
                itemClickListener(place)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int = placeList.size
}
