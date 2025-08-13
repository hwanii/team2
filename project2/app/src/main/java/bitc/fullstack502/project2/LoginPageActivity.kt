package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityLoginPageBinding
import kotlin.jvm.java

class LoginPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시스템 바 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그인 버튼 클릭 이벤트
        binding.loginButton.setOnClickListener {
            val username = binding.insertId.text.toString()
            val password = binding.insertPw.text.toString()

            // 간단 체크 (실제 앱에서는 서버 API로 인증)
            if (username.isNotEmpty() && password.isNotEmpty()) {
                // 1️⃣ 로그인 성공 시 SharedPreferences에 상태 저장
                val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                prefs.edit().putBoolean("logged_in", true).apply() // 로그인 시
                prefs.edit().putBoolean("logged_in", false).apply() // 로그아웃 시

                // 2️⃣ 로그인 성공 후 MyPageActivity로 이동
                val intent = Intent(this, MyPageActivity::class.java)
                startActivity(intent)

                // 3️⃣ LoginPageActivity 종료
                finish()
            } else {
                // 아이디나 비밀번호가 비어있으면 안내
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
