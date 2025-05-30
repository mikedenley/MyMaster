package com.example.swoop

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.GridView

class AutoGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GridView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Make height unconstrained so it expands fully within ScrollView
        val expandSpec = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2,
            MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, expandSpec)
        layoutParams.height = measuredHeight
    }
}
