package ovh.gabrielhuav.flasklogin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ovh.gabrielhuav.flasklogin.data.repository.AuthRepository
import ovh.gabrielhuav.flasklogin.ui.theme.FlaskLoginTheme

sealed class Screen {
    object Login : Screen()
    data class Welcome(val username: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlaskLoginTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val screen = currentScreen) {
                        is Screen.Login -> LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onShowToast = { message ->
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            },
                            onLoginSuccess = { username ->
                                currentScreen = Screen.Welcome(username)
                            }
                        )
                        is Screen.Welcome -> WelcomeScreen(
                            username = screen.username,
                            modifier = Modifier.padding(innerPadding),
                            onLogout = {
                                currentScreen = Screen.Login
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onShowToast: (String) -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Flask Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isLoading,
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = repository.register(username, password)
                        isLoading = false

                        result.onSuccess {
                            onShowToast("✅ ${it.message}")
                        }.onFailure {
                            onShowToast("❌ ${it.message}")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                Text("Registrar")
            }

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = repository.login(username, password)
                        isLoading = false

                        result.onSuccess { response ->
                            val loggedUsername = response.username ?: username
                            onLoginSuccess(loggedUsername)
                        }.onFailure {
                            errorMessage = it.message ?: "Error al iniciar sesión"
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                Text("Iniciar Sesión")
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val result = repository.checkApi()
                    result.onSuccess {
                        onShowToast("API: $it")
                    }.onFailure {
                        onShowToast("Error: ${it.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Probar Conexión API")
        }
    }
}

@Composable
fun WelcomeScreen(
    username: String,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Has iniciado sesión correctamente.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}