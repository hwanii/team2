package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
<<<<<<< HEAD
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
=======
>>>>>>> 65e05a2f466637b0cf2f4b8b4f24f2805d7b5ff4
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityLoginPageBinding
<<<<<<< HEAD

class LoginPageActivity : AppCompatActivity() {

    private lateinit var idEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goJoinText: TextView

    private val repository = LoginRepository() // LoginRepository 구현 필요

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idEditText = findViewById(R.id.user_id)
        pwEditText = findViewById(R.id.user_pw)
        loginButton = findViewById(R.id.loginButton)
        goJoinText = findViewById(R.id.go_join)

        // 로그인 버튼 클릭
        loginButton.setOnClickListener {
            val id = idEditText.text.toString().trim()
            val pw = pwEditText.text.toString().trim()

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "ID와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버 로그인 요청
            repository.loginUser(id, pw) { success, userResponse, message ->
                runOnUiThread {
                    if (success && userResponse != null) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        // 서버에서 받은 실제 데이터를 User 객체로 생성
                        val user = User(
                            userName = userResponse.userName,
                            userId = userResponse.userId,
                            userPw = userResponse.userPw,
                            userTel = userResponse.userTel,
                            userEmail = userResponse.userEmail,
                            userDay = userResponse.userDay
                        )

                        // 화면은 MainActivity로 이동
                        val mainIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainIntent)

                        // 사용자 정보는 EditPageActivity로 전달 준비
                        val editIntent = Intent(this, EditPageActivity::class.java)
                        editIntent.putExtra("user", user)
                        // EditPageActivity에서 Intent로 받아서 사용 가능

                        finish()
                    } else {
                        Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 회원가입 버튼 클릭
        goJoinText.setOnClickListener {
            val intent = Intent(this, JoinPageActivity::class.java)
            startActivity(intent)
        }
=======
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
>>>>>>> 65e05a2f466637b0cf2f4b8b4f24f2805d7b5ff4
    }
}
