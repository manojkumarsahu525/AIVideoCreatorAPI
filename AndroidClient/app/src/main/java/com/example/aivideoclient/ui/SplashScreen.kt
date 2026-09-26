package com.example.aivideoclient.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.aivideoclient.R
import com.example.aivideoclient.datastore.OnboardingDataStore

@Composable
fun SplashScreen(onComplete: (showOnboarding: Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = OnboardingPrefs(context)
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f)

    LaunchedEffect(Unit) {
        visible = true
        delay(1600)
        val first = OnboardingDataStore.isFirstLaunch(context)
        onComplete(first)
    }

    val scaleAnim by animateFloatAsState(targetValue = if (visible) 1f else 0.8f, animationSpec = tween(800))
    val rotation by animateFloatAsState(targetValue = if (visible) 0f else -15f, animationSpec = tween(800))

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Use vector logo if present
            val logo = painterResource(id = R.drawable.ic_logo)
            Image(painter = logo, contentDescription = "App Logo", modifier = Modifier
                .size(120.dp)
                .alpha(alpha)
                .scale(scaleAnim)
                .graphicsLayer(rotationZ = rotation))
            Spacer(Modifier.height(12.dp))
            Text("AI Video Creator", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(alpha))
            Spacer(Modifier.height(8.dp))
            Text("Create · Share · Analyze", modifier = Modifier.alpha(alpha))
        }
    }
}
