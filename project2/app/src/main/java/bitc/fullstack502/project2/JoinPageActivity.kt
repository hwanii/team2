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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinPageActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var checkIdButton: Button
    private lateinit var pwEditText: EditText
    private lateinit var pwCheckEditText: EditText
    private lateinit var telEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var joinButton: Button

    private lateinit var repository: JoinRepository
    private lateinit var binding: ActivityJoinPageBinding
    private var foodList: List<FoodItem>? = null

    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJoinPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameEditText = findViewById(R.id.user_name)
        idEditText = findViewById(R.id.user_id)
        checkIdButton = findViewById(R.id.check_id)
        pwEditText = findViewById(R.id.user_pw)
        pwCheckEditText = findViewById(R.id.check_pw)
        telEditText = findViewById(R.id.user_tel)
        emailEditText = findViewById(R.id.user_email)
        joinButton = findViewById(R.id.summit_btn)

        repository = JoinRepository()

        // 아이디 중복 체크
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

        // 회원가입 버튼
        joinButton.setOnClickListener {
            attemptJoin()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> { startActivity(Intent(this, MainActivity::class.java)); true }
                R.id.menu_list -> { startActivity(Intent(this, ListActivity::class.java)); true }
                R.id.menu_favorite -> {
                    if (!foodList.isNullOrEmpty()) {
                        val mockFavorites = foodList!!.shuffled().take(6)
                        val intent = Intent(this, FavoritesActivity::class.java).apply {
                            putParcelableArrayListExtra("favorites_list", ArrayList(mockFavorites))
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "아직 데이터 로딩 중입니다.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_profile -> startActivity(Intent(this, LoginPageActivity::class.java)).let { true }
                else -> false
            }
        }
        fetchFoodData()
    }
    private fun fetchFoodData() {
        RetrofitClient.api.getFoodList(serviceKey).enqueue(object : Callback<FoodResponse> {
            override fun onResponse(call: Call<FoodResponse>, response: Response<FoodResponse>) {
                if (response.isSuccessful) {
                    val list = response.body()?.getFoodkr?.item?.filter { !it.thumb.isNullOrEmpty() }
                        ?.distinctBy { it.Lat to it.Lng } ?: emptyList()
                    foodList = list
                }
            }
            override fun onFailure(call: Call<FoodResponse>, t: Throwable) {
                Toast.makeText(this@JoinPageActivity, "데이터 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        })
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

        repository.joinUser(name, id, password, tel, email) { success, message ->
            runOnUiThread {
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
