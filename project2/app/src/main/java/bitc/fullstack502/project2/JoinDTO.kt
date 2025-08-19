package bitc.fullstack502.project2

// 회원가입 요청
data class JoinRequest(
    val userName: String,
    val userId: String,
    val userPw: String,
    val userEmail: String,
    val userTel: String
)

// 회원가입 응답
data class JoinResponse(
    val success: Boolean,
    val message: String
)

// 아이디 중복 체크 API 응답 예시
data class IdCheckResponse(
    val available: Boolean,
    val message: String
)
