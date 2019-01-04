package xyz.velvetmilk.testingtool

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
        }
    }
}
