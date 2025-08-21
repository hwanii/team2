package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMyPageBinding

class MyPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPageBinding
    private lateinit var user: User

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedUser = result.data?.getParcelableExtra<User>("updatedUser")
            if (updatedUser != null) {
                user = updatedUser
                updateUI(user)
                saveToPrefs(user)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        user = User(
            userKey = prefs.getInt("user_key", 0),
            userName = prefs.getString("user_name", "") ?: "",
            userId = prefs.getString("user_id", "") ?: "",
            userPw = prefs.getString("user_pw", "") ?: "",
            userTel = prefs.getString("user_tel", "") ?: "",
            userEmail = prefs.getString("user_email", "") ?: ""
        )

        updateUI(user)

        binding.goEdit.setOnClickListener {
            val intent = Intent(this, EditPageActivity::class.java)
            intent.putExtra("user", user)
            editLauncher.launch(intent)
        }

        binding.logoutButton.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }

        val hasReview = false
        binding.myReview.visibility = if (hasReview) View.VISIBLE else View.GONE
        binding.noReview.visibility = if (hasReview) View.GONE else View.VISIBLE
    }

    private fun updateUI(user: User) {
        binding.userName.text = "이름 : ${user.userName}"
        binding.userId.text = "ID : ${user.userId}"
    }

    private fun saveToPrefs(user: User) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("user_name", user.userName)
            putString("user_id", user.userId)
            putString("user_pw", user.userPw)
            putString("user_tel", user.userTel)
            putString("user_email", user.userEmail)
        }.apply()
    }
}
