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

@Composable
fun VideoScreen(viewModel: VideoViewModel, onLogout: () -> Unit) {
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
        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            onLogout()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        videoUrl?.let {
            Spacer(Modifier.height(16.dp))
            VideoPlayer(url = it)
        }
    }
}
