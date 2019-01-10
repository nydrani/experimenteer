package xyz.velvetmilk.testingtool

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_main.view.*


class CustomBehavior : CoordinatorLayout.Behavior<View> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        // layout the children
        parent.onLayoutChild(child, layoutDirection)

        // set recycler view max height to height of parent
        child.recycler_view.setMaxHeight(child.height)

        // Offset the child's current bottom so that its bounds don't overlap the
        // toolbar container.
        val appBarBottom = parent.getDependencies(child)[0].bottom
        ViewCompat.offsetTopAndBottom(child, appBarBottom)

        return true
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        // offset by bottom - current top
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is AppBarLayout.Behavior) {
            ViewCompat.offsetTopAndBottom(child, dependency.bottom - child.top)
        }

        return true
    }
}
