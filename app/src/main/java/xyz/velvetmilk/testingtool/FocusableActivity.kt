package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_focusable.*
import timber.log.Timber
import xyz.velvetmilk.testingtool.tools.keyboardCheckObservable

class FocusableActivity : AppCompatActivity() {

    companion object {
        private val TAG = FocusableActivity::class.simpleName

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
                Timber.d(it.toString())
            }.addTo(disposer)

        fab.setOnClickListener {
            Snackbar.make(it, "hi there", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
