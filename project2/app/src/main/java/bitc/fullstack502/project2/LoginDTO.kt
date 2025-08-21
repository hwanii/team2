package bitc.fullstack502.project2

// 로그인 요청
data class LoginRequest(
    val userId: String,
    val userPw: String
)

// 서버에서 반환하는 사용자 정보
data class UserResponse(
    val userKey: Int,
    val userName: String,
    val userId: String,
    val userPw: String,
    val userTel: String,
    val userEmail: String
)

