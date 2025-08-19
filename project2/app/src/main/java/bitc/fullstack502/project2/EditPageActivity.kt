package bitc.fullstack502.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityEditPageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPageBinding
    private lateinit var repository: EditRepository
    private lateinit var user: User
    private var foodList: List<FoodItem>? = null
    private val serviceKey = "jXBU6vV0oil9ri%2BdWayTquROwX0nqAU70wAnWwE%2BVLyI%2FAIo6iSXppra2iJxeBkscalGGpVa0%2FuTsTOjQ0oQsA%3D%3D"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 전달받은 User 객체
        user = intent.getParcelableExtra<User>("user") ?: return finish()

        repository = EditRepository(RetrofitClient.editApi)

        // 기존 유저 정보 세팅
        loadUserData(user)

        // 수정 버튼 클릭
        binding.editBtn.setOnClickListener {
            attemptEdit()
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
                Toast.makeText(this@EditPageActivity, "데이터 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadUserData(user: User) {
        binding.userName.setText(user.userName)
        binding.userId.setText(user.userId)
        binding.userPw.setText("")
        binding.checkPw.setText("")
        binding.userTel.setText(user.userTel)
        binding.userEmail.setText(user.userEmail)
    }

    private fun attemptEdit() {
        val name = binding.userName.text.toString().trim()
        val id = binding.userId.text.toString().trim()
        val password = binding.userPw.text.toString()
        val passwordCheck = binding.checkPw.text.toString()
        val tel = binding.userTel.text.toString().trim()
        val email = binding.userEmail.text.toString().trim()

        if (name.isEmpty()) { showToast("이름을 입력해주세요."); return }
        if (id.isEmpty()) { showToast("ID를 입력해주세요."); return }
        if (password.isNotEmpty() && password != passwordCheck) { showToast("비밀번호가 일치하지 않습니다."); return }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showToast("올바른 이메일 주소를 입력해주세요."); return }

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
