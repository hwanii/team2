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

class EditPageActivity : BaseActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var pwEditText: EditText
    private lateinit var pwCheckEditText: EditText
    private lateinit var telEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var editButton: Button

    private lateinit var repository: EditRepository
    private lateinit var user: User

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

        fetchFoodData(
            onSuccess = { list ->
            },
            onError = {
                Toast.makeText(this, "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        )

        val bottomNavView = binding.bottomNav.bottomNavigationView
        setupBottomNavigation(bottomNavView)
        val btnSearch = binding.topBar.btnSearch
        setupSearchButton(btnSearch)

        // 로그인 후 전달받은 User 객체
        user = intent.getParcelableExtra<User>("user") ?: return finish()

        nameEditText = findViewById(R.id.user_name)
        idEditText = findViewById(R.id.user_id)
        pwEditText = findViewById(R.id.user_pw)
        pwCheckEditText = findViewById(R.id.check_pw)
        telEditText = findViewById(R.id.user_tel)
        emailEditText = findViewById(R.id.user_email)
        editButton = findViewById(R.id.edit_btn)

        repository = EditRepository(RetrofitClient.editApi)

        loadUserData(user)

        editButton.setOnClickListener {
            attemptEdit()
        }
    }

    private fun loadUserData(user: User) {
        nameEditText.setText(user.userName)
        idEditText.setText(user.userId)
        pwEditText.setText("")
        pwCheckEditText.setText("")
        telEditText.setText(user.userTel)
        emailEditText.setText(user.userEmail)
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

        // 비밀번호가 비어있으면 기존 비밀번호 사용
        val finalPassword = if (password.isEmpty()) user.userPw else password

        repository.updateUser(name, id, finalPassword, tel, email) { success, message ->
            runOnUiThread {
                showToast(message)
                if (success) finish()
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
