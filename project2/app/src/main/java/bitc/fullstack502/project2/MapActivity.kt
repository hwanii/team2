package bitc.fullstack502.project2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import bitc.fullstack502.project2.databinding.ActivityMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding by lazy { ActivityMapBinding.inflate(layoutInflater) }

    private lateinit var marker: Marker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.main) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.main, it).commit()
            }

        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(naverMap: NaverMap) {
        val lat = intent.getFloatExtra("lat",0.0f ).toDouble()
        val lng = intent.getFloatExtra("lng" ,0.0f).toDouble()
        val title = intent.getStringExtra("title")

        val location = LatLng(lat, lng)

        val cameraUpdate = CameraUpdate.scrollAndZoomTo(location,18.0)
        naverMap.moveCamera(cameraUpdate)

        marker = Marker().apply {
            position = location
            map = naverMap
            captionText = title ?: ""
        }
        marker.onClickListener = Overlay.OnClickListener {
            finish()

            true

        }
    }
}