package com.lomon.bubble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            助力人数动态可配置，保证总人数一致
         */

        bpv_left.setProgress(3, 12f)
        bpv_left.setAssistInfo("6-12")


        bpv_right.setProgress(6, 10f)
        bpv_right.setAssistInfo("5-10")

        bpv_bottom.setProgress(9, 12f)
        bpv_bottom.setAssistInfo("3-6-8-12")
    }


}
