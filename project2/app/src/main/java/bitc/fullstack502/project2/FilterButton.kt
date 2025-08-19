package bitc.fullstack502.project2

import android.R.attr.insetLeft
import android.R.attr.insetRight
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class FilterButton(context: Context, attrs: AttributeSet? = null) :
    MaterialButton(context, attrs) {

    init {
        // 최소 크기 제거
        minWidth = 0
        minHeight = 0
        iconPadding = 0

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        setPadding(10, 4, 10, 4)
        params.setMargins(10, 0, 10, 0)
        layoutParams = params
    }

    /** 버튼 ON */
    fun setOnStyle() {
        setTextAppearance(R.style.FilterOnButton)
        setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_orange))
        setTextColor(ContextCompat.getColor(context, R.color.stroke_orange))
        strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width)
        strokeColor = ContextCompat.getColorStateList(context, R.color.stroke_orange)

        setTypeface(typeface, Typeface.BOLD)
    }

    /** 버튼 OFF */
    fun setOffStyle() {
        setTextAppearance(R.style.FilterOffButton)
        setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white))
        setTextColor(ContextCompat.getColor(context, R.color.black))
        strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width)
        strokeColor = ContextCompat.getColorStateList(context, R.color.stroke_gray)

        setTypeface(typeface, Typeface.NORMAL)
    }
}
