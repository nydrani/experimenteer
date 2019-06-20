package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_appbarlayout.*
import timber.log.Timber


class AppBarLayoutTestActivity : AppCompatActivity() {

    companion object {
        fun buildIntent(context: Context): Intent {
            return Intent(context, AppBarLayoutTestActivity::class.java)
        }
    }

    private lateinit var adapter: TestAdapter
    private val disposer = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appbarlayout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TestAdapter()
        adapter.setHasStableIds(true)
        adapter.viewClickSubject.subscribe {
            Timber.d(it.second.toString())
        }.addTo(disposer)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        fab.setOnClickListener {
            Snackbar.make(it, R.string.test_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.test_action) {
                    Toast.makeText(this, R.string.test_message, Toast.LENGTH_SHORT).show()
                }
                .show()

            // add item to adapter
            adapter.addItem(java.util.UUID.randomUUID().toString())
        }

        fab.setOnLongClickListener {
            Snackbar.make(it, "Clearing messages", Snackbar.LENGTH_LONG)
                .setAction(R.string.test_action) {
                    Toast.makeText(this, "Gottem", Toast.LENGTH_SHORT).show()
                }
                .show()

            // clear list of messages
            adapter.updateItems(listOf())
            true
        }
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

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }
}
