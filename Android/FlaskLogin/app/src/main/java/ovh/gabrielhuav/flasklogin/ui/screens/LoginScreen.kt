package ovh.gabrielhuav.flasklogin.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ovh.gabrielhuav.flasklogin.data.exception.NetworkException
import ovh.gabrielhuav.flasklogin.data.repository.AuthRepository

/** Gradient colors for the login background. */
private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val ErrorColor = Color(0xFFFF6B6B)
private val SocialBorderColor = Color.White.copy(alpha = 0.5f)

/** Returns a user-friendly error message for network failures. */
private fun networkErrorMessage(error: Throwable, fallback: String): String = when (error) {
    is NetworkException.ConnectionException -> "🌐 ${error.message}"
    is NetworkException.TimeoutException -> "⏱️ ${error.message}"
    is NetworkException -> error.message ?: "Error de red"
    else -> error.message ?: fallback
}

/**
 * Modern login screen with gradient background, real-time field validation,
 * show/hide password toggle, animated error messages, dark-mode support,
 * social login placeholders, and full accessibility semantics.
 *
 * @param modifier      Modifier applied to the root container.
 * @param onShowToast   Callback to display a transient toast message.
 * @param onLoginSuccess Callback invoked with the authenticated username on success.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onShowToast: (String) -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isValidEmail = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isValidPassword = password.length >= 6
    val isFormValid = isValidEmail && isValidPassword

    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── App icon ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .semantics { contentDescription = "Icono de seguridad" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text = "Bienvenido",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesión en tu cuenta",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Email field ───────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { value ->
                    email = value
                    emailError = value.isNotEmpty() &&
                        !Patterns.EMAIL_ADDRESS.matcher(value).matches()
                },
                label = { Text("Correo Electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = if (emailError) ErrorColor else Color.White
                    )
                },
                isError = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(isError = emailError),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo de correo electrónico" }
            )

            AnimatedVisibility(
                visible = emailError,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = "Ingresa un correo electrónico válido",
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password field ────────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { value ->
                    password = value
                    passwordError = value.isNotEmpty() && value.length < 6
                },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (passwordError) ErrorColor else Color.White
                    )
                },
                trailingIcon = {
                    val description =
                        if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(
                        onClick = { showPassword = !showPassword },
                        modifier = Modifier.semantics { contentDescription = description }
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff
                                          else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(isError = passwordError),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Campo de contraseña" }
            )

            AnimatedVisibility(
                visible = passwordError,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Text(
                    text = "La contraseña debe tener al menos 6 caracteres",
                    color = ErrorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Forgot password link ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .clickable { onShowToast("Función de recuperación próximamente") }
                        .padding(4.dp)
                        .semantics { contentDescription = "Recuperar contraseña" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Login button ──────────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val result = repository.login(email, password)
                        isLoading = false
                        result.onSuccess { response ->
                            onLoginSuccess(response.username ?: email)
                        }.onFailure { error ->
                            onShowToast("❌ ${networkErrorMessage(error, "Error al iniciar sesión")}")
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .semantics { contentDescription = "Botón iniciar sesión" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GradientStart,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GradientStart
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Register button ───────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val result = repository.register(email, password)
                        isLoading = false
                        result.onSuccess { onShowToast("✅ ${it.message}") }
                            .onFailure { error ->
                                onShowToast(
                                    "❌ ${networkErrorMessage(error, "Error al registrar")}"
                                )
                            }
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .semantics { contentDescription = "Botón registrarse" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "  O  ",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Social login buttons ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SocialLoginButton(
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Google") },
                    description = "Iniciar sesión con Google",
                    onClick = { onShowToast("Google login próximamente") }
                )
                SocialLoginButton(
                    icon = { Icon(Icons.Default.People, contentDescription = "Facebook") },
                    description = "Iniciar sesión con Facebook",
                    onClick = { onShowToast("Facebook login próximamente") }
                )
                SocialLoginButton(
                    icon = { Icon(Icons.Default.Language, contentDescription = "Apple") },
                    description = "Iniciar sesión con Apple",
                    onClick = { onShowToast("Apple login próximamente") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── API connection test ───────────────────────────────────────────
            Text(
                text = "Probar conexión API",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            val result = repository.checkApi()
                            result.onSuccess { onShowToast("API: $it") }
                                .onFailure { onShowToast("Error: ${it.message}") }
                        }
                    }
                    .padding(4.dp)
                    .semantics { contentDescription = "Botón probar conexión API" }
            )
        }
    }
}

/** Outlined button used for social login providers. */
@Composable
private fun SocialLoginButton(
    icon: @Composable () -> Unit,
    description: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .semantics { contentDescription = description },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SocialBorderColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        icon()
    }
}

/** Shared [OutlinedTextFieldDefaults.colors] for the login form fields. */
@Composable
private fun fieldColors(isError: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
    cursorColor = Color.White,
    errorBorderColor = ErrorColor,
    errorLabelColor = ErrorColor,
    errorLeadingIconColor = ErrorColor
)
