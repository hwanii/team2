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
  
  private lateinit var binding: ActivityLoginPageBinding
  private val repository = LoginRepository() // LoginRepository 구현 필요
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // 뷰 바인딩 사용
    binding = ActivityLoginPageBinding.inflate(layoutInflater)
    setContentView(binding.root)
    
    // 시스템 인셋 적용 (상단/하단 여백)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    
    // 로그인 버튼 클릭
    binding.loginButton.setOnClickListener {
      val id = binding.userId.text.toString().trim()
      val pw = binding.userPw.text.toString().trim()
      
      if (id.isEmpty() || pw.isEmpty()) {
        Toast.makeText(this, "ID와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      
      // 서버 로그인 요청
      repository.loginUser(id, pw) { success, userResponse, message ->
        runOnUiThread {
          if (success && userResponse != null) {
            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
            
            val user = User(
              userName = userResponse.userName,
              userId = userResponse.userId,
              userPw = userResponse.userPw,
              userTel = userResponse.userTel,
              userEmail = userResponse.userEmail,
              userDay = userResponse.userDay
            )
            
            // MainActivity로 이동
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            
            // EditPageActivity에 전달할 데이터 준비
            val editIntent = Intent(this, EditPageActivity::class.java)
            editIntent.putExtra("user", user)
            
            finish()
          } else {
            Toast.makeText(this, "로그인 실패: $message", Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
    
    // 회원가입 버튼 클릭
    binding.goJoin.setOnClickListener {
      val intent = Intent(this, JoinPageActivity::class.java)
      startActivity(intent)
    }
  }
}
