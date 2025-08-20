// ================================
// SliderAdapter.kt
// RecyclerView 기반 이미지 슬라이드(캐러셀) 어댑터
// 각 슬라이드는 이미지 + 순번 + 텍스트 라벨을 가짐
// 이미지를 클릭하면 SearchActivity로 이동
// ================================
package bitc.fullstack502.project2.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bitc.fullstack502.project2.R
import bitc.fullstack502.project2.SearchActivity

class SliderAdapter(
    private val slides: List<SlideItem>, // 슬라이드 데이터 리스트
    private val context: Context
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    // 뷰홀더: 슬라이드 1장의 구성 요소 (이미지 + 순서 + 라벨)
    inner class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageSlider)   // 메인 이미지
        val orderText: TextView = view.findViewById(R.id.tvSlideOrder)   // 우측 상단 순번 (ex. 1/4)
        val sliderLabel: TextView = view.findViewById(R.id.sliderLabel)  // 이미지 위 텍스트 라벨
    }

    // 레이아웃 inflate 후 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider, parent, false)
        return SliderViewHolder(view)
    }

    // 슬라이드 데이터 바인딩
    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val slide = slides[position]

        // 이미지 세팅
        holder.imageView.setImageResource(slide.imageRes)

        // 우측 상단 순번 표시 (예: "2/4")
        holder.orderText.text = "${position + 1}/${slides.size}"

        // 슬라이드별 텍스트 라벨 (이미지 위에 표시)
        holder.sliderLabel.text = when(position) {
            0 -> "밥 검색"
            1 -> "조개 검색"
            2 -> "찜 검색"
            3 -> "고기 검색"
            else -> ""
        }

        // 이미지 클릭 시 → SearchActivity 이동
        holder.imageView.setOnClickListener {
            // position에 맞는 검색 키워드 설정
            val keyword = when(position) {
                0 -> "밥"
                1 -> "조개"
                2 -> "찜"
                3 -> "고기"
                else -> ""
            }

            // 빈 값이 아닐 경우에만 검색 액티비티 실행
            if (keyword.isNotEmpty()) {
                val intent = Intent(context, SearchActivity::class.java).apply {
                    putExtra("search_keyword", keyword) // 검색 키워드 전달
                }
                context.startActivity(intent)
            }
        }
    }

    // 전체 슬라이드 개수 반환
    override fun getItemCount(): Int = slides.size
}
