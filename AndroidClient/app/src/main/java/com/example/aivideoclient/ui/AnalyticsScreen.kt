package com.example.aivideoclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, onBack: () -> Unit) {
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val analytics by viewModel.analytics.observeAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        error?.let {
            Text(it, color = Color.Red)
        }

        analytics?.let { a ->
            Spacer(Modifier.height(8.dp))
            // Cards row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(title = "Videos This Month", value = a.videosThisMonth.toString(), icon = Icons.Default.BarChart)
                StatCard(title = "Total Time (s)", value = String.format("%.1f", a.totalGenerationTimeMs / 1000.0), icon = Icons.Default.Schedule)
            }

            Spacer(Modifier.height(8.dp))
            StatCard(title = "Plan", value = a.plan ?: "Free", icon = Icons.Default.Subscriptions, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Text("Videos per week", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            WeeklyBarChart(labels = a.weeklyLabels, values = a.weeklyCounts)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(8.dp), elevation = 4.dp, modifier = modifier.weight(1f)) {
        Row(modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Medium)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun WeeklyBarChart(labels: List<String>, values: List<Int>) {
    val maxVal = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        for (i in labels.indices) {
            val v = values.getOrNull(i) ?: 0
            val h = (v.toFloat() / maxVal.toFloat()) * 120.dp.value
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                Box(modifier = Modifier
                    .height((h).dp)
                    .width(24.dp)
                    .background(color = MaterialTheme.colors.primary, shape = RoundedCornerShape(4.dp)))
                Spacer(Modifier.height(4.dp))
                Text(labels[i], fontSize = 12.sp)
            }
        }
    }
}

@Preview
@Composable
fun AnalyticsPreview() {
    val sample = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(
        com.example.aivideoclient.model.AnalyticsResponse(5, 12345.0, "Pro", "2024-12-31", listOf("W1","W2","W3","W4"), listOf(2,3,1,4))
    ) }
    AnalyticsScreen(object : AnalyticsViewModel.Factory(com.example.aivideoclient.repository.AnalyticsRepository(com.example.aivideoclient.network.RetrofitClient.create("https://example.com/"))) {
        // not used in preview
    } as AnalyticsViewModel, onBack = {})
}
