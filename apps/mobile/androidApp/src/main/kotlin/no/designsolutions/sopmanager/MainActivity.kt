package no.designsolutions.sopmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import no.designsolutions.sopmanager.composeapp.AndroidActivityProvider
import no.designsolutions.sopmanager.composeapp.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidActivityProvider.setCurrentActivity(this)
        setContent { App() }
    }

    override fun onDestroy() {
        AndroidActivityProvider.clearActivity(this)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
