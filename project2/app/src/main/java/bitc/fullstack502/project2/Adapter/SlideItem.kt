package bitc.fullstack502.project2.Adapter

data class SlideItem(
    val imageRes: Int,
    val label: String, // 새로 추가된 텍스트 레이블
    val url: String? = null // 기존 URL용도 필요 시
)
