package org.example.project

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ntg.lmd.network.core.KtorClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val secureTokenStore = remember { SecureTokenStoreImpl(context) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val client = KtorClientProvider.create(
        store = secureTokenStore,
        supabaseKey = BuildConfig.SUPABASE_KEY,
        refreshApi = AuthApi(HttpClient(OkHttp)) // simple client just for refresh
    )

    val authApi = AuthApi(client)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val response = authApi.login(username, password) // ApiResponse<LoginData>
                        if (response.success && response.data != null) {
                            //Log.d("Login", "Success: ${response.data.accessToken}")
                            Toast.makeText(context, "Login Success ✅", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, response.error ?: "Invalid username or password", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Login", "Error", e)
                        Toast.makeText(context, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "Login")
        }
    }
}
