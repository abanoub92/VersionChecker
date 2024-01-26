package com.abanoub.versionchecker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.app.ComponentActivity

@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}