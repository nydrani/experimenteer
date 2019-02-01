package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_focusable.*

class FocusableActivity : AppCompatActivity() {

    companion object {
        private val TAG = FocusableActivity::class.java.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, FocusableActivity::class.java)
        }
    }

    private val disposer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focusable)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        keyboardCheckObservable(this).subscribe {
            // clear focus of currently focused
            if (!it) {
                currentFocus?.clearFocus()
            }
            Log.d(TAG, it.toString())
        }.addTo(disposer)
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
