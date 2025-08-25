package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityLoginPageBinding

class LoginPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private val repository = LoginRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그인 버튼 클릭 이벤트
        binding.loginButton.setOnClickListener {
            val id = binding.userId.text.toString().trim()
            val pw = binding.userPw.text.toString().trim()

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "ID와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repository.loginUser(id, pw) { success, userResponse, message ->
                runOnUiThread {
                    if (success && userResponse != null) {
                        Log.d("LoginResponseDebug", "서버 응답: ${userResponse.toString()}")
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()


                        // 로그인 상태 저장
                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putInt("user_key", userResponse.userKey)
                            .putString("user_name", userResponse.userName)
                            .putString("user_id", userResponse.userId)
                            .putString("user_pw", userResponse.userPw)
                            .putString("user_tel", userResponse.userTel)
                            .putString("user_email", userResponse.userEmail)
                            .putInt("user_key", userResponse.userKey)
                            .apply()

                        // 사용자 정보 객체 생성
                        val user = User(
                            userKey = userResponse.userKey,
                            userName = userResponse.userName,
                            userId = userResponse.userId,
                            userPw = userResponse.userPw,
                            userTel = userResponse.userTel,
                            userEmail = userResponse.userEmail
                        )

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("user", user)
                        startActivity(intent)

                        finish()
                    } else {
                        Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 회원가입 이동 버튼 클릭
        binding.goJoin.setOnClickListener {
            val intent = Intent(this, JoinPageActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnClose.setOnClickListener { finish() }

    }
}
