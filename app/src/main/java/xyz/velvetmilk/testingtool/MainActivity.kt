package xyz.velvetmilk.testingtool

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var adapter: TestAdapter
    private val disposer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        fab.setOnClickListener {
            Snackbar.make(it, R.string.test_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.test_action) {
                        Toast.makeText(this, R.string.test_message, Toast.LENGTH_SHORT).show()
                    }
                    .show()

            // add item to adapter
            adapter.addItem(java.util.UUID.randomUUID().toString())
        }

        fab2.setOnClickListener {
            startActivity(AppBarLayoutTestActivity.buildIntent(this))
        }

        fab3.setOnClickListener {
            startActivity(CollapsingToolbarLayoutTestActivity.buildIntent(this))
        }

        fab4.setOnClickListener {
            startActivity(ConstraintBarrierActivity.buildIntent(this))
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_focusable -> {
                    startActivity(FocusableActivity.buildIntent(this))
                    true
                }
                else -> false
            }
        }

        adapter = TestAdapter()
        adapter.setHasStableIds(true)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.viewClickSubject.subscribe {
            Log.d(TAG, it.second.toString())
        }.addTo(disposer)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposer.clear()
    }
}
