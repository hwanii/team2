package bitc.fullstack502.project2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import bitc.fullstack502.project2.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        val addr = intent.getStringExtra("addr")
        val subaddr = intent.getStringExtra("subaddr")
        val tel = intent.getStringExtra("tel")
        val time = intent.getStringExtra("time")
        val item = intent.getStringExtra("item")
        val imageurl = intent.getStringExtra("imageurl")


        binding.txtTitle.text = title
        binding.txtAddr.text = addr
        binding.txtSubAddr.text = subaddr
        binding.txtTel.text = tel
        binding.txtTime.text = time
        binding.txtItem.text = item

        Glide.with(this)
            .load(imageurl)
            .into(binding.txtImage)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}