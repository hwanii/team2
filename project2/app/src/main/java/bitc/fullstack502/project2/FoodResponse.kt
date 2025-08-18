package bitc.fullstack502.project2

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serial

data class FoodResponse(
    @SerializedName("getFoodKr")
    val getFoodkr : FoodResult?
)

data class  FoodResult(
    val totalCount:Int,
    val pageNo:Int,
    val numOfRows: Int,
    val item: List<FoodItem>
)
@Parcelize
data class  FoodItem(
    @SerializedName("MAIN_IMG_THUMB")
    val thumb : String?,
    @SerializedName("MAIN_IMG_NORMAL")
    val image: String?,
    @SerializedName("UC_SEQ")
    val UcSeq: Int,

    val TITLE: String,
    @SerializedName("ADDR1")
    val ADDR: String,
    @SerializedName("ADDR2")
    val SubAddr: String?,
    @SerializedName("CNTCT_TEL")
    val TEL: String?,
    val GUGUN_NM : String,
    @SerializedName("USAGE_DAY_WEEK_AND_TIME")
    val Time: String?,
    @SerializedName("ITEMCNTNTS")
    val Item : String?,
    @SerializedName("LAT")
    val Lat : Float?,
    @SerializedName("LNG")
    val Lng : Float?,
    val MAIN_TITLE : String,
    
    @SerializedName("RPRSNTV_MENU")
    val CATE_NM : String?,
    @SerializedName("MAIN_IMG_THUMB")
    val MAIN_IMG : String,
): Parcelable

// 임시 데이터! 확인용
data class Review(
    val rating: Float,
    val content: String,
    val date: String
)

