package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import xyz.velvetmilk.testingtool.viewmodels.FlowViewModel
import kotlin.random.Random

class FlowActivity : AppCompatActivity() {

    companion object {
        private val TAG = FlowActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, FlowActivity::class.java)
        }
    }

    private lateinit var viewModel: FlowViewModel
    private lateinit var disposer: CompositeDisposable

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(FlowViewModel::class.java)
        disposer = CompositeDisposable()

        // observe the livedata
        viewModel.countData.observe(this) {
            base_view.text = it.toString()
        }

        fab.setOnClickListener {
            base_view.text = Random.nextInt().toString()
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
