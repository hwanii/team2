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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.view.View

class PlaceAdapter(
    private val placeList: List<Place>,
    private val itemClickListener: (Place) -> Unit,
    private val onBookmarkClick: (Place, Boolean) -> Unit,
    private val bookmarkedPlaceIds: MutableSet<Long> = mutableSetOf()
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Place) {
            binding.tvTitle.text = place.title

            // 즐겨찾기
            val isBookmarked = bookmarkedPlaceIds.contains(place.id)
            updateBookmarkIcon(isBookmarked)

            // 하트 버튼 클릭 이벤트
            binding.bookMark.setOnClickListener {
                val newState = !bookmarkedPlaceIds.contains(place.id)
                if (newState) {
                    bookmarkedPlaceIds.add(place.id)
                } else {
                    bookmarkedPlaceIds.remove(place.id)
                }
                updateBookmarkIcon(newState)
                onBookmarkClick(place, newState) // 변경 알림
            }

            val rating = place.rating
            val category = place.category
            val menu = place.menu.replace("\n", " ")

            if (rating > 0) {
                val ratingText = rating.toString()
                val text = "  $ratingText · $category · $menu" // 공백 2칸은 별 아이콘 자리
                val spannable = SpannableString(text)

                // 별 아이콘
                val starDrawable = binding.root.context.getDrawable(R.drawable.ic_star)
                starDrawable?.setBounds(0, 0, starDrawable.intrinsicWidth, starDrawable.intrinsicHeight)
                val imageSpan = CenterAlignImageSpan(starDrawable!!)
                spannable.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // rating 부분만 Bold + 검정
                val start = 2
                val end = start + ratingText.length
                spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                binding.tvAddr.text = spannable
            } else {
                // 별점이 없으면 그냥 카테고리·메뉴만 표시
                binding.tvAddr.text = "$category · $menu"
            }

            // 운영 시간
            val rawTime = place.time ?: ""
            val cleanedTime = cleanRawTime(rawTime)
            val statusText = getStoreStatus(cleanedTime)

            val displayText = if (statusText.isNotEmpty()) "$statusText  •  $cleanedTime" else cleanedTime
            val timeSpannable = SpannableString("  $displayText")

            // 시계 아이콘
            val clockDrawable = binding.root.context.getDrawable(R.drawable.ic_time)
            clockDrawable?.setBounds(0, 0, clockDrawable.intrinsicWidth, clockDrawable.intrinsicHeight)
            timeSpannable.setSpan(CenterAlignImageSpan(clockDrawable!!), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // 줄바꿈 후 들여쓰기
            val marginPx = 48
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

        // 즐겨찾기
        private fun updateBookmarkIcon(isBookmarked: Boolean) {
            if (isBookmarked) {
                binding.bookMark.setImageResource(R.drawable.ic_heart_on)
            } else {
                binding.bookMark.setImageResource(R.drawable.ic_heart)
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

        // 첫 번째 아이템에만 상단 구분선 표시
        if (position == 0) {
            holder.binding.topDivider.visibility = View.VISIBLE
        } else {
            holder.binding.topDivider.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = placeList.size


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

        // 문장 중간에 의도치 않은 줄바꿈이 있으면 모두 공백으로 변경
        text = text.replace(Regex("\\s*\\n\\s*"), " ")


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

        // 요일, 오픈/마감/휴업 등 포함 시 영업상태 표시 안함
        val blockWords = listOf("월", "화", "수", "목", "금", "토", "일", "오픈", "마감", "휴업")
        if (blockWords.any { rawTime.contains(it) }) return ""

        val now = LocalTime.now()
        if (rawTime.contains("24시간") || rawTime.contains("00:00-24:00")) return "영업중"

        // ---- 라스트오더(Last Order) 시간 추출 ----
        val lastOrderRegex = Regex("\\((\\d{1,2}:\\d{2})\\s*라스트오더\\)")
        val lastOrderMatch = lastOrderRegex.find(rawTime)
        val lastOrderTime = lastOrderMatch?.groupValues?.getOrNull(1)?.let { parseTime(it) }

        // ---- 일반 오픈/마감 시간 파싱 ----
        val timeRegex = Regex("(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})")
        val ranges = timeRegex.findAll(rawTime).mapNotNull {
            val start = parseTime(it.groupValues[1]) ?: return@mapNotNull null
            var end = parseTime(it.groupValues[2]) ?: return@mapNotNull null

            // 라스트오더 시간이 존재하고, 마감 시간보다 빠르면 end 시간을 교체
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


    private fun parseTime(time: String, ampm: String?): LocalTime? {
        return try {
            var t = time.trim().lowercase(Locale.getDefault())
            val parts = t.split(":")
            if (parts.size != 2) return null
            var hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null

            // am/pm 처리
            if (!ampm.isNullOrEmpty()) {
                val a = ampm.lowercase(Locale.getDefault())
                if (a.contains("pm") && hour in 1..11) hour += 12
                if (a.contains("am") && hour == 12) hour = 0
            }

            // 24:00 처리 → 23:59로 변경
            if (hour == 24) {
                hour = 23
                return LocalTime.of(hour, 59)
            }

            if (hour !in 0..23 || minute !in 0..59) return null
            LocalTime.of(hour, minute)
        } catch (_: Exception) {
            null
        }
    }


}
