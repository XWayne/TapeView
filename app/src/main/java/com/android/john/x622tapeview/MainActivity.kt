package com.android.john.x622tapeview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.john.tapeview.UpdateListen
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tapeView.setUpdateListener(updateListen)
    }

    private val updateListen:UpdateListen = {value->
        textView.text = "$value"
    }
}
