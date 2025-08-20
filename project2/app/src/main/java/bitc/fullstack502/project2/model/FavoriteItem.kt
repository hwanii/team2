package bitc.fullstack502.project2.model

data class FavoriteItem(
  val favoriteKey: Int,
  val userKey: Int,
  val placeCode: Int,
  val isFavorite: Int,
  val favDate: String,
  val updateDate: String
)