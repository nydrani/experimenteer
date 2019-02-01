package xyz.velvetmilk.testingtool

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import io.reactivex.Observable


fun dpToPx(context: Context, valueInDp: Float): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
}


fun keyboardCheckObservable(activity: Activity) : Observable<Boolean> {
    val contentView = activity.findViewById<View>(android.R.id.content)

    return Observable.create<Boolean> {
        // NOTE: May need to remove global layout listener?
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect().apply {
                contentView.getWindowVisibleDisplayFrame(this)
            }

            val heightDiff = contentView.rootView.height - (r.bottom - r.top)
            if (heightDiff > dpToPx(activity, 200.0f)) { // if more than 200 dp, it's probably a keyboard...
                it.onNext(true)
            } else {
                it.onNext(false)
            }
        }
    }.distinctUntilChanged()
}