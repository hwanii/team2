package bitc.fullstack502.project2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userName: String,
    val userId: String,
    val userPw: String,
    val userTel: String,
    val userEmail: String
) : Parcelable
