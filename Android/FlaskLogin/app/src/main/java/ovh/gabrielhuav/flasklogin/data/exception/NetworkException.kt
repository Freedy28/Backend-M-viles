package ovh.gabrielhuav.flasklogin.data.exception

sealed class NetworkException(message: String) : Exception(message) {
    /** No hay conectividad o la conexión fue rechazada (p. ej. Docker apagado). */
    class ConnectionException(message: String = "No hay conexión de red. Verifica tu conexión a internet e intenta de nuevo.") :
        NetworkException(message)

    /** La solicitud superó el tiempo de espera. */
    class TimeoutException(message: String = "El servidor no responde. Intenta de nuevo más tarde.") :
        NetworkException(message)

    /** Error HTTP con código de estado (credenciales inválidas, usuario duplicado, etc.). */
    class HttpException(val code: Int, message: String) : NetworkException(message)

    /** Cualquier otro error de red no clasificado. */
    class UnknownNetworkException(message: String = "Error de red desconocido. Intenta de nuevo.") :
        NetworkException(message)
}
