package com.hardik.the_server_driven_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.hardik.the_server_driven_ui.static.StaticLandingScreen
import com.hardik.the_server_driven_ui.ui.theme.TheServerDrivenUITheme

/**
 * Cold-launch entry point for the hardcoded twin (Part 2, PERF.md).
 * Launched standalone via `adb shell am start -n
 * com.hardik.the_server_driven_ui/.StaticMainActivity` so its TTR/TTI
 * are measured from a real cold start, same as [MainActivity].
 */
class StaticMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheServerDrivenUITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StaticLandingScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
