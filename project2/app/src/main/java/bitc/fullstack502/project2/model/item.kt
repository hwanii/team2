    package bitc.fullstack502.project2.model

    import android.os.Parcelable
    import kotlinx.parcelize.Parcelize

    /**
     * 앱에서 공통으로 사용할 음식점 데이터 모델
     * @param title 음식점 이름
     * @param rating 별점 (0.0 ~ 5.0) - 공공데이터에는 없으므로 기본 0.0
     * @param category 대표 메뉴
     * @param address 주소
     * @param thumbUrl 썸네일 이미지 URL
     */
    // model/Item.kt
    // model/Item.kt


    @Parcelize
    data class Item(
        val title: String,
        val rating: Double,
        val category: String,
        val address: String,
        val thumbUrl: String
    ) : Parcelable