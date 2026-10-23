package com.yarik.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yarik.android.ui.theme.YarikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YarikTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StarterHomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StarterHomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = "Hello Yarik!",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Mesh background service is running. Chat UI: TODO — wire :yarik-mesh or a new screen.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StarterHomePreview() {
    YarikTheme {
        StarterHomeScreen()
    }
}
