package com.example.aivideoclient.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegister: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.Center) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@Button
            }
            loading = true
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    loading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                        // fetch ID token and store for API calls
                        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                            com.example.aivideoclient.utils.TokenStore.idToken = result.token
                        }
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Signing in..." else "Sign In")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.End)) {
            Text("Register")
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), verticalArrangement = Arrangement.Center) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@Button
            }
            loading = true
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    loading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                        val ctx = context.applicationContext
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            com.example.aivideoclient.utils.TokenRepository.refreshToken(ctx)
                        }
                        onRegisterSuccess()
                    } else {
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Creating..." else "Register")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
            Text("Cancel")
        }
    }
}
