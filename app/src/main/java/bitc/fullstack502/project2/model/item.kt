package bitc.fullstack502.project2.model


/**
 * 앱에서 공통으로 사용할 아이템 데이터 모델
 * @param title 음식점 이름 또는 아이템 이름
 * @param rating 별점 (0.0 ~ 5.0)
 * @param category 카테고리명
 * @param address 주소
 */
data class Item(
    val title: String,
    val rating: Double,
    val category: String,
    val address: String
)