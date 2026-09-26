package com.example.aivideoclient.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.aivideoclient.datastore.OnboardingDataStore

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val pages = listOf(
        OnboardPage("AI video generation", "Generate videos from text prompts using AI.", Icons.Default.AutoAwesome),
        OnboardPage("Subscriptions", "Subscribe for higher quality and faster generation.", Icons.Default.Lock),
        OnboardPage("Analytics", "View usage and performance analytics.", Icons.Default.Analytics),
        OnboardPage("Sharing", "Share videos to social platforms.", Icons.Default.Share)
    )

    val pagerState = rememberPagerState(initialPage = 0)
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        HorizontalPager(pageCount = pages.size, state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val p = pages[page]
            val alpha by animateFloatAsState(if (pagerState.currentPage == page) 1f else 0.5f)
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(p.icon, contentDescription = null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(96.dp).alpha(alpha))
                Spacer(Modifier.height(16.dp))
                Text(p.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(p.description, color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp), lineHeight = 20.sp)
            }
        }

        // Indicators
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            for (i in pages.indices) {
                val selected = pagerState.currentPage == i
                Box(modifier = Modifier
                    .size(if (selected) 12.dp else 8.dp)
                    .padding(4.dp)
                    .background(color = if (selected) MaterialTheme.colors.primary else Color.LightGray, shape = RoundedCornerShape(6.dp)))
            }
        }

        Spacer(Modifier.height(16.dp))
        val scope = rememberCoroutineScope()
        Button(onClick = {
            scope.launch {
                OnboardingDataStore.setLaunched(context)
                onGetStarted()
            }
        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            Text("Get Started")
        }
        Spacer(Modifier.height(8.dp))
    }
}

data class OnboardPage(val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
