package bitc.fullstack502.project2

import android.graphics.Typeface
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.databinding.ItemFoodBinding
import bitc.fullstack502.project2.model.FavoriteItem
import com.bumptech.glide.Glide
import java.time.LocalTime
import java.util.Locale

class PlaceAdapter(
    private val foodList: MutableList<FoodItem>,
    private val itemClickListener: (FoodItem) -> Unit,
    private val onBookmarkClick: (FoodItem, Boolean) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
    
    inner class PlaceViewHolder(val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FoodItem) {
            binding.tvTitle.text = item.TITLE
            updateBookmarkIcon(item.isBookmarked)
            
            val status = getStoreStatus(item.Time ?: "")
            binding.tvTime.text = status
            
            binding.bookMark.setOnClickListener {
                val newState = !item.isBookmarked
                item.isBookmarked = newState
                updateBookmarkIcon(newState)
                onBookmarkClick(item, newState)
            }
            
            val category = item.GUGUN_NM
            val menu = item.CATE_NM?.replace("\n", " ") ?: ""
            binding.tvAddr.text = "$category · $menu"
            
            Glide.with(binding.root.context)
                .load(item.image)
                .into(binding.ivFoodImage)
            
            binding.root.setOnClickListener { itemClickListener(item) }
        }
        
        private fun updateBookmarkIcon(isBookmarked: Boolean) {
            binding.bookMark.setImageResource(
                if (isBookmarked) R.drawable.ic_heart_on
                else R.drawable.ic_heart
            )
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlaceViewHolder(ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) =
        holder.bind(foodList[position])
    
    override fun getItemCount(): Int = foodList.size
    
    /** 서버에서 가져온 즐겨찾기 placeCode로 상태 반영 */
    fun updateBookmarkState(favoritePlaceCodes: Set<Int>) {
        foodList.forEach { item ->
            item.isBookmarked = favoritePlaceCodes.contains(item.UcSeq)
        }
        notifyDataSetChanged()
    }

    // --------------------- 영업시간 처리 ---------------------
    private fun cleanRawTime(rawTime: String): String {
        var text = rawTime.trim()

        // 모든 종류의 공백/줄바꿈을 하나로 통일
        text = text.replace(Regex("[\\s\\u00A0\\u2000-\\u200B\\u2028\\u2029]+"), " ")

        // "24 시간" 같이 공백과 줄바꿈이 섞인 경우 통일
        text = text.replace(Regex("24\\s*\\n*\\s*시간"), "24시간")

        // 상태 표시도 공백/줄바꿈 제거 후 붙이기
        val statusList = listOf("영업중", "영업전", "영업후", "영업마감")
        for (status in statusList) {
            text = text.replace(Regex("${status}\\s*\\n*\\s*"), "$status ")
        }

        // 문장 중간에 의도치 않은 줄바꿈 → 공백으로 변경
        text = text.replace(Regex("\\s*\\n\\s*"), " ")

        // 휴업 기간 제거
        text = text.replace(
            Regex("\\d{2}\\.\\s*\\d{2}\\.\\s*\\d{2}\\s*~\\s*\\d{2}\\.\\s*\\d{2}\\.\\s*\\d{2}\\s*휴업중"),
            ""
        )

        text = text.replace("~", "-")
        text = text.replace(" - ", "-")
        text = text.replace(Regex("(?i)(a\\.m\\.|p\\.m\\.|am|pm)"), "")

        return text.trim()
    }

    private fun getStoreStatus(rawTime: String): String {
        if (rawTime.isBlank()) return ""

        // 요일, 오픈/마감/휴업 포함 → 상태 표시 안함
        val blockWords = listOf("월", "화", "수", "목", "금", "토", "일", "오픈", "마감", "휴업")
        if (blockWords.any { rawTime.contains(it) }) return ""

        val now = LocalTime.now()
        if (rawTime.contains("24시간") || rawTime.contains("00:00-24:00")) return "영업중"

        val lastOrderRegex = Regex("\\((\\d{1,2}:\\d{2})\\s*라스트오더\\)")
        val lastOrderMatch = lastOrderRegex.find(rawTime)
        val lastOrderTime = lastOrderMatch?.groupValues?.getOrNull(1)?.let { parseTime(it) }

        val timeRegex = Regex("(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})")
        val ranges = timeRegex.findAll(rawTime).mapNotNull {
            val start = parseTime(it.groupValues[1]) ?: return@mapNotNull null
            var end = parseTime(it.groupValues[2]) ?: return@mapNotNull null

            if (lastOrderTime != null && lastOrderTime.isBefore(end)) {
                end = lastOrderTime
            }
            start to end
        }.toList()

        if (ranges.isEmpty()) return ""

        val inOpen = ranges.any { inRange(now, it.first, it.second) }
        val inFuture = ranges.any { now.isBefore(it.first) }

        return when {
            inOpen -> "영업중"
            inFuture -> "영업전"
            else -> "영업마감"
        }
    }

    private fun parseTime(time: String): LocalTime? {
        return try {
            val parts = time.trim().split(":")
            if (parts.size != 2) return null
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()
            if (hour == 24) hour = 23
            LocalTime.of(hour, minute)
        } catch (_: Exception) {
            null
        }
    }

    private fun inRange(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return !now.isBefore(start) && !now.isAfter(end)
    }
}
