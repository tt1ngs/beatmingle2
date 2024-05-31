package com.ttings.beatwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.ttings.beatwave.navigation.AppNavigation
import com.ttings.beatwave.ui.theme.BeatwaveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Beatwave)
        setContent {
            BeatwaveTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}