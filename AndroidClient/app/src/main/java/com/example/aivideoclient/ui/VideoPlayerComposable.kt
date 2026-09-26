package com.example.aivideoclient.ui

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height

@Composable
fun VideoPlayer(url: String, modifier: Modifier = Modifier.fillMaxWidth().height(240.dp)) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(url) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(factory = { ctx ->
        PlayerView(ctx).apply {
            player = exoPlayer
            useController = true
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }, modifier = modifier)
}
