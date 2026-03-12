package ovh.gabrielhuav.flasklogin.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: String,
    val message: String,
    @SerializedName("user_id")
    val userId: Int? = null,
    val username: String? = null
)