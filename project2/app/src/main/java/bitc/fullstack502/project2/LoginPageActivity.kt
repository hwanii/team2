package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityLoginPageBinding

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
    }
}
