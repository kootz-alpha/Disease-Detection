package abhishekh.diseasedetector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class MainActivity : AppCompatActivity() {

    private var TIME_OUT = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Set up a handler to post a runnable that will start the next activity after a delay
        Handler().postDelayed({

            val startAppIntent: Intent = Intent(this, HomePage::class.java)

            startActivity(startAppIntent)
            finish()
        }, TIME_OUT)
    }
}