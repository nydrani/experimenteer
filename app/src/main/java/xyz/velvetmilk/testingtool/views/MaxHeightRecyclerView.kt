package xyz.velvetmilk.testingtool.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


class MaxHeightRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    private var maxHeight = -1

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var newHeightSpec = heightSpec
        val mode = MeasureSpec.getMode(heightSpec)
        val height = MeasureSpec.getSize(heightSpec)

        if (maxHeight >= 0 && (mode == MeasureSpec.UNSPECIFIED || height > maxHeight)) {
            newHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, newHeightSpec)
    }

    /**
     * Sets the maximum height for this recycler view.
     */
    fun setMaxHeight(newMaxHeight: Int) {
        if (maxHeight != newMaxHeight) {
            maxHeight = newMaxHeight
            //requestLayout()
        }
    }
}
