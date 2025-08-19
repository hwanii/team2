package bitc.fullstack502.project2.data

data class Place(
    val id: Long,
    val title: String,    // 매장명
    val rating: Double,   // 별점
    val addr: String,     // 주소
    val category: String, // 카테고리 (구)
    val menu: String, // 대표메뉴
    val time: String,     // 운영시간
    val imageUrl: String  // 이미지 주소
)
