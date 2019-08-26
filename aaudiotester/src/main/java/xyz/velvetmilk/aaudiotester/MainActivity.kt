package xyz.velvetmilk.aaudiotester

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import xyz.velvetmilk.aaudiotester.jni.AAudioJniLib

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.simpleName

        fun buildIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private val aaudioJNILib = AAudioJniLib()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            aaudioJNILib.createEngine()
        }

        fab2.setOnClickListener {
            aaudioJNILib.shutdown()
        }

        button1.setOnClickListener {
            aaudioJNILib.getState()
        }

        button2.setOnClickListener {
            aaudioJNILib.play()
        }

        button3.setOnClickListener {
            aaudioJNILib.pause()
        }

        button4.setOnClickListener {
            aaudioJNILib.stop()
        }
    }
}
