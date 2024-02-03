package at.faymann.tgtgscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import at.faymann.tgtgscanner.ui.TgtgScannerApp
import at.faymann.tgtgscanner.ui.TgtgScannerViewModel
import at.faymann.tgtgscanner.ui.theme.TgtgScannerTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TgtgScannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TgtgScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TgtgScannerApp()
                }
            }
        }
    }
}