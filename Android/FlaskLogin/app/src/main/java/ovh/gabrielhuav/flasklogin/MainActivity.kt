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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ovh.gabrielhuav.flasklogin.ui.screens.LoginScreen
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
