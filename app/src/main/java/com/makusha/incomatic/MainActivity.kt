package com.makusha.incomatic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.makusha.incomatic.design.IncomaticTheme
import com.makusha.incomatic.shell.IncomaticShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IncomaticTheme {
                IncomaticShell()
            }
        }
    }
}
