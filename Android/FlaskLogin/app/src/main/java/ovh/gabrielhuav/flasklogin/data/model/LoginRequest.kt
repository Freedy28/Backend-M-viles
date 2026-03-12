package ovh.gabrielhuav.flasklogin.data.model

data class LoginRequest(
    val username: String,
    val password: String
)