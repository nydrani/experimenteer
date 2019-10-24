package xyz.velvetmilk.testingtool

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_system.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import xyz.velvetmilk.testingtool.di.ActivityModule
import xyz.velvetmilk.testingtool.di.DaggerActivityComponent
import xyz.velvetmilk.testingtool.jni.ExternalJniLib
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SystemActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private val TAG = SystemActivity::class.simpleName
        private const val SERVER_URL = "http://192.168.105.14:3000/"

        fun buildIntent(context: Context): Intent {
            return Intent(context, SystemActivity::class.java)
        }
    }

    interface NetworkService {
        data class TestResponse(val message: String)

        @GET("/external")
        @Streaming
        suspend fun externalGet(): ResponseBody
    }

    @Inject
    lateinit var gsonConverterFactory: GsonConverterFactory
    @Inject
    lateinit var scalarsConverterFactory: ScalarsConverterFactory
    @Inject
    lateinit var okhttpClient: OkHttpClient

    private lateinit var service: NetworkService

    private lateinit var disposer: CompositeDisposable
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    @SuppressLint("UnsafeDynamicallyLoadedCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        job = Job()
        disposer = CompositeDisposable()

        // dagger injection
        DaggerActivityComponent.factory()
            .create((application as TestingApp).appComponent, ActivityModule(this))
            .inject(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .client(okhttpClient)
            .addConverterFactory(scalarsConverterFactory)
            .addConverterFactory(gsonConverterFactory)
            .build()

        service = retrofit.create(NetworkService::class.java)

        fab.setOnClickListener {
            val stringBuilder = StringBuilder()

            stringBuilder.appendln(System.currentTimeMillis())
            stringBuilder.appendln(System.getProperties())
            stringBuilder.appendln(System.getenv())
            stringBuilder.appendln(System.getSecurityManager())
            stringBuilder.appendln(System.nanoTime())

            base_view.text = stringBuilder.toString()
        }

        fab2.setOnClickListener {
            val externalJniLib = ExternalJniLib(this, true)
            base_view.text = externalJniLib.ping().toString()
        }

        fab3.setOnClickListener {
            try {
                val externalJniLib = ExternalJniLib(this, false)
                base_view.text = externalJniLib.ping().toString()
            } catch (e: UnsatisfiedLinkError) {
                base_view.text = e.localizedMessage
            }
        }

        fab4.setOnClickListener {
            launch(Dispatchers.IO) {
                try {
                    val stream = service.externalGet().byteStream()
                    // write bytes to file
                    File(filesDir, "libexternal.so")
                        .outputStream()
                        .use { stream.copyTo(it) }
                    launch(Dispatchers.Main) {
                        base_view.text = "done"
                    }
                } catch (e: IOException) {
                    // io exception
                    e.printStackTrace()
                } catch (e: HttpException) {
                    // http exception
                    e.printStackTrace()
                }
            }
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
