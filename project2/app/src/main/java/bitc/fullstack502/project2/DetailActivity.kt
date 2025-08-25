package bitc.fullstack502.project2

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import bitc.fullstack502.project2.databinding.ActivityDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern



class DetailActivity : AppCompatActivity() {


    private var isLike = false
    private var userKey: Int = -1


    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    private var placeCode: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userKey = sharedPreferences.getInt("user_key", -1)

        checkLoginStatus()


        val currentItem: FoodItem? = intent.getParcelableExtra("clicked_item")
        val allItems: ArrayList<FoodItem>? = intent.getParcelableArrayListExtra("full_list")
        if (currentItem == null) {
            finish()
            return
        }
        displayCurrentItemDetails(currentItem)


        if(!allItems.isNullOrEmpty()) {
            setupRecommendations(currentItem, allItems)
        }


        placeCode = currentItem.UcSeq?.toInt() ?: -1

        if (userKey != -1 && placeCode != -1) {
            checkFavoriteStatus()
        }
        if (placeCode != -1) {
            loadReviewsFromServer(placeCode)
        }

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBarInput.rating
            val content = binding.editTextReview.text.toString().trim()

            // 1. SharedPreferences에서 사용자 키(user_key) 가져오기
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val userKey = sharedPreferences.getInt("user_key", -1)

            // 3. 리뷰 내용이 비어있는지 확인
            if (content.isEmpty()) {
                Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("ReviewDebug", "전송하려는 placeCode: $placeCode")
            Log.d("ReviewDebug", "전송하려는 userKey: $userKey")
            // 4. 서버로 보낼 데이터 객체 생성 (1단계에서 만든 ReviewRequest 사용)
            val reviewRequest = ReviewRequest(
                userKey = userKey,
                placeCode = placeCode,
                reviewNum  = rating,
                reviewItem  = content
            )

            // 5. Retrofit을 사용해 서버에 리뷰 전송 (2단계에서 만든 submitReview 함수 사용)
            RetrofitClient.reviewApi.createReview(reviewRequest).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailActivity, "리뷰가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.editTextReview.text.clear()
                        loadReviewsFromServer(placeCode)
                    } else {
                        Toast.makeText(this@DetailActivity, "리뷰 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("ReviewError", "서버 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@DetailActivity, "리뷰 등록 실패 (네트워크 오류)", Toast.LENGTH_SHORT).show()
                    Log.e("ReviewError", "네트워크 실패", t)
                }
            })
        }
        
        // ===== 뒤로가기 버튼 클릭 리스너 설정 =====
        binding.btnBack.setOnClickListener {
            // 이전 화면으로 돌아감
            finish()
        }
        
        // ===== 홈 버튼 클릭 리스너 설정 =====
        binding.btnHome.setOnClickListener {
            // MainActivity (홈 화면)으로 이동
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            
            finish()
        }
    }

    private fun cleanMenuText(menu: String?): String {
        if (menu.isNullOrBlank()) {
            return "" // 원본이 비어있으면 빈 텍스트 반환
        }
        Log.d("MenuDebug", "Original Text: [$menu]")
        Log.d("MenuDebug", "Original Text: [$title]")

        var cleanedText = menu

        cleanedText = cleanedText.replace(Regex("\\(.*?\\)"), "")
        cleanedText = cleanedText.replace(Regex("[\\s=]*[₩￦][\\s=]*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("-[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("/\\s*[\\d,]+"), "")
        cleanedText = cleanedText.replace(Regex("\\d+g"), "")

        // 3. 줄바꿈(\n)을 쉼표와 공백으로 변경
        cleanedText = cleanedText.replace("\n", ", ")

        // 4. 앞뒤 공백 및 여러 개의 공백을 하나로 정리
        val menuItems = cleanedText.split(Regex("[,\\s]+")).filter { it.isNotBlank() }

        // 3. 나눠진 메뉴 리스트에서 최대 2개만 선택합니다.
        val limitedMenuItems = menuItems.take(2)

        // 4. 선택된 메뉴들을 ", "로 연결하여 최종 문자열을 만듭니다.
        return limitedMenuItems.joinToString(", ")
    }

//        val title = intent.getStringExtra("title")
//        val addr = intent.getStringExtra("addr")
//        val subaddr = intent.getStringExtra("subaddr")
//        val tel = intent.getStringExtra("tel")
//        val time = intent.getStringExtra("time")
//        val item = intent.getStringExtra("item")
//        val imageurl = intent.getStringExtra("imageurl")
//         val GUGUN_NM = intent.getStringExtra("gugum")
//        val lat = intent.getFloatExtra("lat",0.0f).toDouble()
//        val lng = intent.getFloatExtra("lng", 0.0f).toDouble()

    private fun displayCurrentItemDetails(item: FoodItem) {

        val cleanedMenu = cleanMenuText(item.CATE_NM)

        binding.txtTitle.text = " ${item.TITLE }"
        binding.txtAddr.text = "\uD83D\uDCCD ${item.ADDR }"
//        binding.txtSubAddr.text = "${item.SubAddr}"
        binding.txtTel.text = "\uD83D\uDCDE ${item.TEL}"
        binding.txtTime.text = getOperatingStatus(item.Time)
        binding.txtItem.text = "⸰ ${item.Item}"
        binding.txtAddrcategory.text = "${item.GUGUN_NM} > $cleanedMenu"


        Glide.with(this)
            .load(item.image)
            .into(binding.txtImage)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("lat", item.Lat)
                putExtra("lng", item.Lng)
                putExtra("title", item.TITLE)
                putExtra("addr", item.ADDR)

            }
            startActivity(intent)
        }
        binding.btnLike.setOnClickListener {
            if (userKey == -1) {
                Toast.makeText(this, "로그인이 필요한 기능입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // isLiked 상태에 따라 즐겨찾기 추가 또는 삭제 함수 호출
            toggleFavorite()
        }
    }
    private fun checkFavoriteStatus() {
        RetrofitClient.favoritesApi.getFavorites(userKey).enqueue(object : Callback<List<Int>> {
            override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                if (response.isSuccessful) {
                    val favoritePlaceCodes = response.body()
                    // 서버에서 받은 즐겨찾기 목록에 현재 장소가 포함되어 있는지 확인
                    isLike = favoritePlaceCodes?.contains(placeCode) == true
                    updateLikeButtonUI() // UI 업데이트
                } else {
                    Log.e("DetailActivity", "Failed to check favorite status: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                Log.e("DetailActivity", "Network error while checking favorite status", t)
            }
        })
    }

    // 5. (추가) isLiked 상태에 따라 하트 아이콘을 업데이트하는 함수
    private fun updateLikeButtonUI() {
        if (isLike) {
            binding.btnLike.setImageResource(R.drawable.heart_full)
        } else {
            binding.btnLike.setImageResource(R.drawable.heart_none)
        }
    }
    private fun toggleFavorite() {
        val body = mapOf("userKey" to userKey, "placeCode" to placeCode)

        // 현재 '좋아요'가 아닌 상태 -> '좋아요' 추가 API 호출
        val call: Call<Void> = if (!isLike) {
            RetrofitClient.favoritesApi.addFavorite(body)
        } else { // 현재 '좋아요' 상태 -> '좋아요' 삭제 API 호출
            RetrofitClient.favoritesApi.removeFavorite(body)
        }

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 요청 성공 시, isLiked 상태를 반전시키고 UI 업데이트
                    isLike = !isLike
                    updateLikeButtonUI()
                    val message = if (isLike) "즐겨찾기에 추가되었습니다." else "즐겨찾기에서 삭제되었습니다."
                    Toast.makeText(this@DetailActivity, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DetailActivity, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("DetailActivity", "Favorite toggle network error", t)
            }
        })
    }

    // AM/PM이 포함된 시간을 24시간제 텍스트로 변환하는 필수 헬퍼 함수
    private fun convertAmPmTo24Hour(timePart: String): String {
        val trimmedPart = timePart.trim().lowercase(Locale.ENGLISH)
        val pattern = Pattern.compile("(am|pm)?\\s*(\\d{1,2}):(\\d{2})")
        val matcher = pattern.matcher(trimmedPart)

        if (matcher.find()) {
            val amPm = matcher.group(1)
            var hour = matcher.group(2)?.toInt() ?: 0
            val minute = matcher.group(3)

            if (amPm == "pm" && hour < 12) {
                hour += 12
            }
            if (amPm == "am" && hour == 12) {
                hour = 0
            }
            return String.format("%02d:%s", hour, minute)
        }
        return trimmedPart // 변환할 패턴이 없으면 원본에서 공백만 제거해서 반환
    }

    // 모든 케이스를 처리하는 최종 통합 함수
    private fun getOperatingStatus(timeString: String?): String {
        if (timeString.isNullOrBlank()) {
            return "⸰ 정보 없음"
        }

        try {
            var isOpen = false
            var targetString = timeString // 분석할 대상 문자열

            // 1. 요일 정보가 있는지 먼저 확인
            val containsDayInfo = listOf("월", "화", "수", "목", "금", "토", "일").any { timeString.contains(it) }

            if (containsDayInfo) {
                val today = LocalDate.now().dayOfWeek
                val lines = timeString.split("\n")
                var operatingHoursLine: String? = null

                for (line in lines) {
                    val dayPart = line.split(":")[0].trim()
                    if (dayPart.contains("-")) {
                        val days = dayPart.split("-")
                        if (days.size < 2) continue
                        val dayMap = mapOf("월" to DayOfWeek.MONDAY, "화" to DayOfWeek.TUESDAY, "수" to DayOfWeek.WEDNESDAY, "목" to DayOfWeek.THURSDAY, "금" to DayOfWeek.FRIDAY, "토" to DayOfWeek.SATURDAY, "일" to DayOfWeek.SUNDAY)
                        val startDay = dayMap[days[0]]
                        val endDay = dayMap[days[1]]
                        if (startDay != null && endDay != null && today.value in startDay.value..endDay.value) {
                            operatingHoursLine = line
                            break
                        }
                    } else if (line.contains(today.getDisplayName(java.time.format.TextStyle.NARROW, Locale.KOREAN))) {
                        operatingHoursLine = line
                        break
                    }
                }
                // 오늘 영업 정보가 있으면 분석 대상을 해당 라인으로 변경, 없으면 영업 종료 처리
                if (operatingHoursLine != null) {
                    targetString = operatingHoursLine
                } else {
                    return "🔴 [영업중단] $timeString"
                }
            }

            // 2. 분석 대상 문자열(targetString)에서 시간 패턴 추출
            val timePattern = Pattern.compile("((?:am|pm)?\\s*\\d{1,2}:\\d{2})\\s*[-~]\\s*((?:am|pm)?\\s*\\d{1,2}:\\d{2})")
            val breakPattern = Pattern.compile("(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})\\s*브레이크")

            val timeMatcher = timePattern.matcher(targetString)
            val breakMatcher = breakPattern.matcher(timeString)

            if (timeMatcher.find()) {
                // 3. AM/PM을 24시간제로 변환
                val startTimeStr = convertAmPmTo24Hour(timeMatcher.group(1))
                val endTimeStr = convertAmPmTo24Hour(timeMatcher.group(2))

                val formatter = DateTimeFormatter.ofPattern("H:mm")
                val startTime = LocalTime.parse(startTimeStr, formatter)
                val endTime = LocalTime.parse(endTimeStr, formatter)
                val now = LocalTime.now()

                val isInOperatingHours = if (startTime.isAfter(endTime)) {
                    now.isAfter(startTime) || now.isBefore(endTime)
                } else {
                    now.isAfter(startTime) && now.isBefore(endTime)
                }
                var isInBreakTime = false
                if (breakMatcher.find()) {
                    val breakStartTime = LocalTime.parse(breakMatcher.group(1), formatter)
                    val breakEndTime = LocalTime.parse(breakMatcher.group(2), formatter)
                    isInBreakTime = now.isAfter(breakStartTime) && now.isBefore(breakEndTime)
                }
                isOpen = isInOperatingHours && !isInBreakTime
            }
            
            return if (isOpen) {
                "영업중  •  $timeString"
            } else {
                "영업마감  •  $timeString"
            }

        } catch (e: Exception) {
            return "⸰ $timeString"
        }
    }
    private fun setupRecommendations(currentItem: FoodItem, allItems: List<FoodItem>) {
        // 같은 '구/군'의 다른 맛집들을 필터링하고, 순서를 섞은 후, 최대 3개만 가져옵니다.
        val currentLoc = Location("current")
        val currentLat = currentItem.Lat?.toDouble()
        val currentLng = currentItem.Lng?.toDouble()
        val recommendations: List<FoodItem>

        if (currentLat != null && currentLng != null) {
            currentLoc.latitude = currentLat
            currentLoc.longitude = currentLng

            recommendations = allItems
                .filter { it.UcSeq != currentItem.UcSeq } // 자기 자신 제외
                .mapNotNull { foodItem ->
                    // 추천 후보의 위치 정보가 유효할 때만 거리를 계산
                    val lat = foodItem.Lat?.toDouble()
                    val lng = foodItem.Lng?.toDouble()
                    if (lat != null && lng != null) {
                        val itemLoc = Location("item")
                        itemLoc.latitude = lat
                        itemLoc.longitude = lng
                        val distance = currentLoc.distanceTo(itemLoc)
                        // 거리 제한 조건 (예: 10km 이내)
                        if (distance < 20000) {
                            Pair(foodItem, distance)
                        } else {
                            null
                        }
                    } else {
                        null // 위치 정보 없는 후보는 제외
                    }
                }
                .sortedBy { it.second } // 가까운 순으로 정렬
                .take(3) // 상위 3개 선택
                .map { it.first } // 맛집 정보만 추출

        } else {
            // 3. 현재 맛집 위치 정보가 없으면 '같은 구/군' 랜덤 추천으로 대체
            recommendations = allItems
                .filter { it.GUGUN_NM == currentItem.GUGUN_NM && it.UcSeq != currentItem.UcSeq }
                .shuffled()
                .take(3)
        }

        // 4. 최종 추천 목록을 화면에 표시
        if (recommendations.isNotEmpty()) {
            bindRecommendationData(recommendations[0], allItems, binding.recommend1, binding.recommendImage1, binding.recommendTitle1, binding.recommendGugun1)
        }
        if (recommendations.size > 1) {
            bindRecommendationData(recommendations[1], allItems, binding.recommend2, binding.recommendImage2, binding.recommendTitle2, binding.recommendGugun2)
        }
        if (recommendations.size > 2) {
            bindRecommendationData(recommendations[2], allItems, binding.recommend3, binding.recommendImage3, binding.recommendTitle3, binding.recommendGugun3)
        }
    }

    // 추천 데이터를 각 뷰에 바인딩하는 헬퍼 함수
    private fun bindRecommendationData(
        item: FoodItem,
        allItem: List<FoodItem>,
        container: LinearLayout,
        imageView: ImageView,
        titleView: TextView,
        gugunView: TextView,
    ) {
        container.visibility = View.VISIBLE // 숨겨진 뷰를 보이게 함
        titleView.text = item.TITLE
        gugunView.text = item.GUGUN_NM
        Glide.with(this).load(item.thumb).into(imageView)

        container.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("clicked_item",item)
                putParcelableArrayListExtra("full_list", ArrayList(allItem))
            }
            startActivity(intent)
        }
    }

    private fun loadReviewsFromServer(placeCode: Int) {
        val call = RetrofitClient.reviewApi.getReviews(placeCode)

        // API 비동기 실행
        call.enqueue(object : Callback<List<ReviewResponse>> {

            // API 호출 성공 시 실행되는 함수
            override fun onResponse(
                call: Call<List<ReviewResponse>>,
                response: Response<List<ReviewResponse>>
            ) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    if (!reviews.isNullOrEmpty()) {
                        createReviewsDynamically(reviews)
                    }
                } else {
                    Log.e("DetailActivity", "리뷰 로딩 실패: ${response.code()}")
                }
            }

            // API 호출 실패 시 실행되는 함수
            override fun onFailure(call: Call<List<ReviewResponse>>, t: Throwable) {
                Log.e("DetailActivity", "리뷰 로딩 네트워크 오류", t)
            }
        })
    }
    private fun createReviewsDynamically(reviews: List<ReviewResponse>) {
        val reviewsContainer = binding.reviewsContainer
        reviewsContainer.removeAllViews()

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val loggedInUserKey = sharedPreferences.getInt("user_key", -1)
        reviews.forEach { reviewItem ->
            val reviewLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(0, 0, 0, 24)
                }
            }

            val topRowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val starIcon = ImageView(this).apply {
                setImageResource(R.drawable.star)
                val iconSize = 40 // 아이콘 크기 조절
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            }

            // ❗️ reviewItem.rating -> reviewItem.reviewRating 으로 수정
            val ratingText = TextView(this).apply {
                text = reviewItem.reviewRating.toString()
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(8, 0, 16, 0)
            }

            // ❗️ reviewItem.date -> reviewItem.reviewDay 로 수정
            val dateText = TextView(this).apply {
                text = reviewItem.reviewDay.replace("T", " ")
            }

            topRowLayout.addView(starIcon)
            topRowLayout.addView(ratingText)
            topRowLayout.addView(dateText)

            if (reviewItem.userKey == loggedInUserKey) {
                // 공간을 채울 Spacer View 추가
                val spacer = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0, 0, 1.0f // weight 1
                    )
                }
                topRowLayout.addView(spacer)

                val editButton = TextView(this)
                val deleteButton = TextView(this)
                val cancelButton = TextView(this)

                cancelButton.apply {
                    text = "수정 취소"
                    setPadding(16, 0, 0, 0)
                    visibility = View.GONE
                    setOnClickListener {
                        deleteButton.visibility = View.VISIBLE
                        editButton.visibility = View.VISIBLE
                        cancelButton.visibility = View.GONE

                        binding.reviewInputSection.visibility = View.VISIBLE
                        binding.editTextReview.text.clear()
                        binding.ratingBarInput.rating = 5.0f
                        binding.btnSubmitReview.text = "리뷰 등록"
                        setSubmitReviewClickListener()
                    }
                    topRowLayout.addView(cancelButton)
                }

                // 수정 버튼 추가
                editButton.apply {
                    text = "수정"
                    setPadding(16, 0, 0, 0)
                    setOnClickListener {

                        editButton.visibility = View.GONE
                        deleteButton.visibility = View.GONE
                        cancelButton.visibility = View.VISIBLE

                        // 리뷰 입력 섹션을 보이게 하고 값 채우기
                        binding.reviewInputSection.visibility = View.VISIBLE
                        binding.editTextReview.setText(reviewItem.reviewItem)
                        binding.ratingBarInput.rating = reviewItem.reviewRating
                        binding.btnSubmitReview.text = "리뷰 수정"

                        // "리뷰 수정" 버튼에 대한 새 클릭 리스너 설정
                        binding.btnSubmitReview.setOnClickListener {
                            val updatedContent = binding.editTextReview.text.toString().trim()
                            val updatedRating = binding.ratingBarInput.rating

                            if (updatedContent.isEmpty()) {
                                Toast.makeText(this@DetailActivity, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            val updateRequest = ReviewUpdateRequest(
                                reviewItem = updatedContent,
                                reviewNum = updatedRating
                            )

                            RetrofitClient.reviewApi.updateReview(reviewItem.reviewKey, updateRequest).enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@DetailActivity, "리뷰가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                        binding.editTextReview.text.clear()
                                        binding.reviewsContainer.visibility = View.VISIBLE
                                        binding.btnSubmitReview.text = "리뷰 등록"
                                        setSubmitReviewClickListener()
                                        loadReviewsFromServer(placeCode)
                                    } else {
                                        Toast.makeText(this@DetailActivity, "수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Toast.makeText(this@DetailActivity, "네트워크 오류로 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                }
                topRowLayout.addView(editButton)
                // 삭제 버튼 추가
                deleteButton.apply {
                    text = "삭제"
                    setPadding(16, 0, 0, 0)

                    setOnClickListener {
                        // ✅ 확인 창을 띄웁니다.
                        androidx.appcompat.app.AlertDialog.Builder(this@DetailActivity)
                            .setTitle("리뷰 삭제")
                            .setMessage("정말로 이 리뷰를 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                // "삭제" 버튼을 누르면 API 호출
                                val reviewKey = reviewItem.reviewKey
                                RetrofitClient.reviewApi.deleteReview(reviewKey).enqueue(object : Callback<String> {
                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(this@DetailActivity, "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                            // 리뷰 목록을 새로고침하여 화면에서 즉시 사라지게 합니다.
                                            loadReviewsFromServer(placeCode)
                                        } else {
                                            Toast.makeText(this@DetailActivity, "삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        Toast.makeText(this@DetailActivity, "네트워크 오류로 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                }
                topRowLayout.addView(deleteButton)
            }




            // ❗️ reviewItem.content -> reviewItem.reviewItem 으로 수정
            val contentText = TextView(this).apply {
                text = reviewItem.reviewItem
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.setMargins(0, 8, 0, 0) // 위쪽 여백 추가
                }
                setPadding(24, 24, 24, 24) // 패딩 추가
                background = getDrawable(R.drawable.border_bg)
            }

            reviewLayout.addView(topRowLayout)
            reviewLayout.addView(contentText)

            reviewsContainer.addView(reviewLayout)
        }


    }
    private fun setSubmitReviewClickListener() {
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBarInput.rating
            val content = binding.editTextReview.text.toString().trim()

            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val userKey = sharedPreferences.getInt("user_key", -1)

            if (content.isEmpty()) {
                Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.d("ReviewDebug", "전송하려는 placeCode: $placeCode")
            Log.d("ReviewDebug", "전송하려는 userKey: $userKey")

            val reviewRequest = ReviewRequest(
                userKey = userKey,
                placeCode = placeCode,
                reviewNum  = rating,
                reviewItem  = content
            )

            RetrofitClient.reviewApi.createReview(reviewRequest).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailActivity, "리뷰가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.editTextReview.text.clear()
                        loadReviewsFromServer(placeCode)
                    } else {
                        Toast.makeText(this@DetailActivity, "리뷰 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        Log.e("ReviewError", "서버 응답 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@DetailActivity, "리뷰 등록 실패 (네트워크 오류)", Toast.LENGTH_SHORT).show()
                    Log.e("ReviewError", "네트워크 실패", t)
                }
            })
        }
    }
    private fun checkLoginStatus() {
        // SharedPreferences에서 토큰을 가져오는 로직 (로그인 구현 시 만드셨을 Util 클래스 등 활용)
        // 예시: val token = App.prefs.token
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        // ❗️ 2. "auth_token" 대신 "isLoggedIn" 값을 확인 (기본값은 false)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            binding.reviewInputSection.visibility = View.VISIBLE
        } else {
            binding.reviewInputSection.visibility = View.GONE
        }
    }
}




