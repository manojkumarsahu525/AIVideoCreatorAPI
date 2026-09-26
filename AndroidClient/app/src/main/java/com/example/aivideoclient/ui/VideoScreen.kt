package com.example.aivideoclient.ui

package com.example.aivideoclient.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.widget.Toast
import java.io.File

@Composable
fun VideoScreen(viewModel: VideoViewModel, onLogout: () -> Unit, onAnalytics: () -> Unit) {
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val videoUrl by viewModel.videoUrl.observeAsState()

    var prompt by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Prompt") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { viewModel.generate(prompt) },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Generating...")
            } else {
                Text("Generate Video")
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAnalytics, modifier = Modifier.weight(1f)) {
                Text("Analytics")
            }

            Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            onLogout()
            }, modifier = Modifier.weight(1f)) {
                Text("Logout")
            }
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        videoUrl?.let {
            Spacer(Modifier.height(16.dp))
            VideoPlayer(url = it)

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    // Share video URL or local file
                    val ctx = context
                    try {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "video/*"
                        }

                        if (it.startsWith("http://") || it.startsWith("https://")) {
                            // Share URL as text
                            intent.type = "text/plain"
                            intent.putExtra(Intent.EXTRA_TEXT, it)
                        } else {
                            // Treat as local file path
                            val file = if (it.startsWith("file://")) File(Uri.parse(it).path!!) else File(it)
                            if (file.exists()) {
                                val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                                intent.putExtra(Intent.EXTRA_STREAM, uri)
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } else {
                                // fallback: share as text
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, it)
                            }
                        }

                        val chooser = Intent.createChooser(intent, "Share video")
                        // verify there's an app to receive the intent
                        if (chooser.resolveActivity(ctx.packageManager) != null) {
                            ctx.startActivity(chooser)
                        } else {
                            Toast.makeText(ctx, "No app available to share", Toast.LENGTH_SHORT).show()
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Unable to share: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Share")
                }
            }
        }
    }
}
