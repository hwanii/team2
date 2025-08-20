package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMyPageBinding

class MyPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val user = User(
            userName = prefs.getString("user_name", "") ?: "",
            userId = prefs.getString("user_id", "") ?: "",
            userPw = prefs.getString("user_pw", "") ?: "",
            userTel = prefs.getString("user_tel", "") ?: "",
            userEmail = prefs.getString("user_email", "") ?: ""
        )

        // 화면에 유저 정보 표시
        if (user.userName.isNotEmpty() && user.userId.isNotEmpty()) {
            binding.userName.text = "이름 : ${user.userName}"
            binding.userId.text = "ID : ${user.userId}"
        } else {
            Log.d("MyPageActivity", "User info not found in SharedPreferences")
        }

        // 프로필 수정 버튼 클릭
        binding.goEdit.setOnClickListener {
            val intent = Intent(this, EditPageActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        // 로그아웃 버튼 클릭
        binding.logoutButton.setOnClickListener {
            prefs.edit().clear().apply()  // 모든 사용자 정보 삭제
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }

        val hasReview = false

        if (hasReview) {
            binding.myReview.visibility = View.VISIBLE
            binding.noReview.visibility = View.GONE
        } else {
            binding.myReview.visibility = View.GONE
            binding.noReview.visibility = View.VISIBLE
        }
    }
}
