package xyz.velvetmilk.testingtool

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

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

        adapter = TestAdapter()
        adapter.setHasStableIds(true)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}
