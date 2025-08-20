package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityJoinPageBinding
import kotlin.jvm.java

class JoinPageActivity : BaseActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var checkIdButton: Button
    private lateinit var pwEditText: EditText
    private lateinit var pwCheckEditText: EditText
    private lateinit var telEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var joinButton: Button

    private lateinit var repository: JoinRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityJoinPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchFoodData(
            onSuccess = { list ->
            },
            onError = {
                Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )

        val bottomNavView = binding.baseLayout.bottomNavigationView
        setupBottomNavigation(bottomNavView)

        nameEditText = findViewById(R.id.user_name)
        idEditText = findViewById(R.id.user_id)
        checkIdButton = findViewById(R.id.check_id)
        pwEditText = findViewById(R.id.user_pw)
        pwCheckEditText = findViewById(R.id.check_pw)
        telEditText = findViewById(R.id.user_tel)
        emailEditText = findViewById(R.id.user_email)
        joinButton = findViewById(R.id.summit_btn)

        repository = JoinRepository()

        checkIdButton.setOnClickListener {
            val id = idEditText.text.toString().trim()
            if (id.isEmpty()) {
                showToast("ID를 입력해주세요.")
                return@setOnClickListener
            }
            repository.checkIdDuplicate(id) { available, message ->
                runOnUiThread {
                    if (available) {
                        showToast("사용 가능한 ID입니다.")
                    } else {
                        showToast("사용 불가능한 ID입니다: $message")
                    }
                }
            }
        }

        joinButton.setOnClickListener {
            attemptJoin()
        }
    }

    private fun attemptJoin() {
        val name = nameEditText.text.toString().trim()
        val id = idEditText.text.toString().trim()
        val password = pwEditText.text.toString()
        val passwordCheck = pwCheckEditText.text.toString()
        val tel = telEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()

        if (name.isEmpty()) {
            showToast("이름을 입력해주세요.")
            return
        }
        if (id.isEmpty()) {
            showToast("ID를 입력해주세요.")
            return
        }
        if (password.isEmpty()) {
            showToast("비밀번호를 입력해주세요.")
            return
        }
        if (password != passwordCheck) {
            showToast("비밀번호가 일치하지 않습니다.")
            return
        }
        if (tel.isEmpty()) {
            showToast("전화번호를 입력해주세요")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("올바른 이메일 주소를 입력해주세요.")
            return
        }

        // 실제 회원가입 요청 호출
        repository.joinUser(name, id, password, tel, email) { success, message ->
            runOnUiThread {
                // 회원가입 성공 시
                if (success) {
                    showToast("회원가입 성공!")

                    val intent = Intent(this, LoginPageActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("회원가입 실패: $message")
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
