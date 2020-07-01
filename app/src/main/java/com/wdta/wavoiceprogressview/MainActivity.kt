package com.wdta.wavoiceprogressview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wdta.widgt.waprogressview.RecordVoiceProgressListener

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WARecordVoiceProgressView.recordVoiceProgressListener = recordVoiceProgressListener
        GlobalScope.launch {
            delay(1000)
            WARecordVoiceProgressView.start()
        }
    }

    private var recordVoiceProgressListener = object : RecordVoiceProgressListener {
        override fun recordFinish() {
            Log.d("DEBUG","recordFinish")
        }
    }
}
