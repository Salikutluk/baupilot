package de.baupilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.baupilot.designsystem.Roboto
import de.baupilot.designsystem.Tokens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RootScreen() }
    }
}

@Composable
fun RootScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Tokens.bg.color()),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Baupilot", fontFamily = Roboto)
    }
}
