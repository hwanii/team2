package bitc.fullstack502.project2

import com.google.gson.annotations.SerializedName

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

data class  FoodItem(
    @SerializedName("MAIN_IMG_NORMAL")
    val image: String?,

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
    val Item : String?
)





