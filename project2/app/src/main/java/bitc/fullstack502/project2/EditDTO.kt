package bitc.fullstack502.project2

data class EditRequest(
    val userName: String,
    val userId: String,
    val userPw: String,
    val userTel: String,
    val userEmail: String
)

data class EditResponse(
    val success: Boolean,
    val message: String
)
