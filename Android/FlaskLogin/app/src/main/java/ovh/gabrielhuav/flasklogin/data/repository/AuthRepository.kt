package ovh.gabrielhuav.flasklogin.data.repository

import ovh.gabrielhuav.flasklogin.data.api.RetrofitClient
import ovh.gabrielhuav.flasklogin.data.exception.NetworkException
import ovh.gabrielhuav.flasklogin.data.model.LoginRequest
import ovh.gabrielhuav.flasklogin.data.model.LoginResponse
import ovh.gabrielhuav.flasklogin.data.model.RegisterRequest
import ovh.gabrielhuav.flasklogin.data.model.RegisterResponse
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepository {

    private val apiService = RetrofitClient.authApiService

    /** Convierte excepciones de bajo nivel en [NetworkException] con mensajes amigables. */
    private fun Exception.toNetworkException(): NetworkException = when (this) {
        is NetworkException -> this
        is SocketTimeoutException -> NetworkException.TimeoutException()
        is ConnectException, is UnknownHostException ->
            NetworkException.ConnectionException()
        else -> NetworkException.UnknownNetworkException(
            message ?: "Error de red desconocido. Intenta de nuevo."
        )
    }

    suspend fun checkApi(): Result<String> {
        return try {
            val response = apiService.checkApiStatus()
            if (response.isSuccessful) {
                val message = response.body()?.get("message") ?: "API funcionando"
                Result.success(message)
            } else {
                Result.failure(
                    NetworkException.HttpException(response.code(), "Error: ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e.toNetworkException())
        }
    }

    suspend fun register(username: String, password: String): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(username, password)
            val response = apiService.register(request)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "El usuario ya existe"
                    else -> "Error al registrar: ${response.code()}"
                }
                Result.failure(NetworkException.HttpException(response.code(), errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e.toNetworkException())
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                response.body()?.let {
                    if (it.status == "success") {
                        Result.success(it)
                    } else {
                        Result.failure(Exception(it.message))
                    }
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Credenciales inválidas"
                    else -> "Error al iniciar sesión: ${response.code()}"
                }
                Result.failure(NetworkException.HttpException(response.code(), errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e.toNetworkException())
        }
    }
}