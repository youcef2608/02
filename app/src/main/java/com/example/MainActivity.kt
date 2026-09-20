package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.AtharApp
import com.example.ui.theme.AtharTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.data.repository.AtharRepository.initContext(applicationContext)
        setContent {
            AtharTheme {
                AtharApp()
            }
        }
    }
}
