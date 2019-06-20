package xyz.velvetmilk.testingtool

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_material.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


class MaterialActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = MaterialActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, MaterialActivity::class.java)
        }
    }

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        val tempUnits = arrayOf("Celcius", "Kelvin", "Fahrenheit")
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown_menu_popup,
            tempUnits)

        dropdown_text.setAdapter(adapter)
        dropdown_text.keyListener = null
        dropdown_text.setText(tempUnits[0], false)

        dropdown_text.setOnItemClickListener { _, _, position, id ->
            Timber.d("onItemClick")
            Timber.d(position.toString())
            Timber.d(id.toString())
        }
        dropdown_text.setOnDismissListener {
            currentFocus?.clearFocus()
        }

        fab.setOnClickListener {
            base_view.text = Random.nextInt().toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
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
