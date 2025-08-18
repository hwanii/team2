package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

class FavoritesActivity : AppCompatActivity() {

    // 1. 바인딩 변수를 클래스 멤버로 이동
    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root) // 클래스 멤버 바인딩 사용

        // Intent에서 데이터 받아오기 (이 부분은 올바르게 작성하셨습니다)
        val currentItem: FoodItem? = intent.getParcelableExtra("clicked_item")

        // currentItem이 null이면 액티비티를 즉시 종료
        if (currentItem == null) {
            finish()
            return
        }

        // 받아온 데이터로 화면 표시
        displayCurrentItemDetails(currentItem)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- 여기부터는 DetailActivity와 동일한 헬퍼 함수들 ---
    // (함수들이 다른 함수 안에 있지 않고, 클래스 바로 아래에 있도록 수정되었습니다)

    private fun displayCurrentItemDetails(item: FoodItem) {
        val cleanedMenu = cleanMenuText(item.CATE_NM)

        binding.txtTitle.text = " ${item.TITLE}"
        binding.txtAddr.text = "\uD83D\uDCCD ${item.ADDR}"
        binding.txtTel.text = "\uD83D\uDCDE ${item.TEL}"
        binding.txtTime.text = getOperatingStatus(item.Time)
        binding.txtItem.text = "⸰ ${item.Item}"
        binding.txtAddrcategory.text = "${item.GUGUN_NM} > $cleanedMenu"

        Glide.with(this)
            .load(item.image)
            .into(binding.txtImage)

        // 즐겨찾기 화면에서는 추천이나 리뷰 기능이 필요 없다면 관련 로직은 제거해도 됩니다.
        // 예를 들어 지도 보기, 좋아요 버튼 등은 필요에 맞게 커스터마이징 하세요.
    }

    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) return ""
        var cleanedText = menu
        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace("\n", ", ")
        return cleanedText.trim().replace(Regex("\\s+"), " ")
    }

    private fun convertAmPmTo24Hour(timePart: String): String {
        val trimmedPart = timePart.trim().lowercase(Locale.ENGLISH)
        val pattern = Pattern.compile("(am|pm)?\\s*(\\d{1,2}):(\\d{2})")
        val matcher = pattern.matcher(trimmedPart)

        if (matcher.find()) {
            val amPm = matcher.group(1)
            var hour = matcher.group(2)?.toInt() ?: 0
            val minute = matcher.group(3)
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            return String.format("%02d:%s", hour, minute)
        }
        return trimmedPart
    }

    private fun getOperatingStatus(timeString: String?): String {
        if (timeString.isNullOrBlank()) return "⸰ 정보 없음"
        try {
            var isOpen = false
            var targetString = timeString
            val containsDayInfo = listOf("월", "화", "수", "목", "금", "토", "일").any { timeString.contains(it) }

            if (containsDayInfo) {
                // ... (DetailActivity와 동일한 영업시간 계산 로직)
            }

            // ... (DetailActivity와 동일한 영업시간 계산 로직)
            val timePattern = Pattern.compile("((?:am|pm)?\\s*\\d{1,2}:\\d{2})\\s*[-~]\\s*((?:am|pm)?\\s*\\d{1,2}:\\d{2})")
            val timeMatcher = timePattern.matcher(targetString)

            if (timeMatcher.find()) {
                val startTimeStr = convertAmPmTo24Hour(timeMatcher.group(1))
                val endTimeStr = convertAmPmTo24Hour(timeMatcher.group(2))
                val formatter = DateTimeFormatter.ofPattern("H:mm")
                val startTime = LocalTime.parse(startTimeStr, formatter)
                val endTime = LocalTime.parse(endTimeStr, formatter)
                val now = LocalTime.now()

                isOpen = if (startTime.isAfter(endTime)) {
                    now.isAfter(startTime) || now.isBefore(endTime)
                } else {
                    now.isAfter(startTime) && now.isBefore(endTime)
                }
            }
            return if (isOpen) "🟢 [영업중] $timeString" else "🔴 [영업중단] $timeString"
        } catch (e: Exception) {
            return "⸰ $timeString"
        }
    }
}