package bitc.fullstack502.project2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityEditPageBinding

class EditPageActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var pwCheckEditText: EditText
    private lateinit var telEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var editButton: Button

    // Retrofit과 연결된 Repository
    private val repository = EditRepository(RetrofitClient.JoinApiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityEditPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nameEditText = findViewById(R.id.user_name)
        idEditText = findViewById(R.id.user_id)
        pwEditText = findViewById(R.id.user_pw)
        pwCheckEditText = findViewById(R.id.check_pw)
        telEditText = findViewById(R.id.user_tel)
        emailEditText = findViewById(R.id.user_email)
        editButton = findViewById(R.id.edit_btn)

        loadUserData()

        editButton.setOnClickListener {
            attemptEdit()
        }
    }

    private fun loadUserData() {
        nameEditText.setText("홍길동")
        idEditText.setText("user123")
        pwEditText.setText("")
        pwCheckEditText.setText("")
        telEditText.setText("01012345678")
        emailEditText.setText("test@test.com")
    }

    private fun attemptEdit() {
        val name = nameEditText.text.toString().trim()
        val id = idEditText.text.toString().trim()
        val password = pwEditText.text.toString()
        val passwordCheck = pwCheckEditText.text.toString()
        val tel = telEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (name.isEmpty()) { showToast("이름을 입력해주세요."); return }
        if (id.isEmpty()) { showToast("ID를 입력해주세요."); return }
        if (password.isNotEmpty() && password != passwordCheck) { showToast("비밀번호가 일치하지 않습니다."); return }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showToast("올바른 이메일 주소를 입력해주세요."); return }

        // 서버 요청
        repository.updateUser(name, id, password, tel, email) { success, message ->
            runOnUiThread {
                showToast(message)
                if (success) finish() // 성공하면 화면 종료
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}