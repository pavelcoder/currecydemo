package ru.pavelcoder.modulbankdemo.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager

class WrapHeightViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var localHeightMeasureSpec = heightMeasureSpec
        var height = 0
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val h: Int = child.measuredHeight
            if (h > height) height = h
        }
        if (height != 0) {
            localHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, localHeightMeasureSpec)
    }
}