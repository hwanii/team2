package bitc.fullstack502.project2

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class CenterAlignImageSpan(drawable: Drawable) : ImageSpan(drawable) {
    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.save()

        val transY = ((bottom - top) - drawable.bounds.bottom) / 2 + top
        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}
