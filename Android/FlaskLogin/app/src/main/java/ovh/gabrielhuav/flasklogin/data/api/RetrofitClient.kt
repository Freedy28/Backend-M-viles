package ovh.gabrielhuav.flasklogin.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANTE: Cambia esta URL según tu entorno
    // Para emulador Android: "http://10.0.2.2:5000/"
    // Para dispositivo físico: "http://TU_IP_LOCAL:5000/"
    // Para producción: "https://tu-dominio.com/"
    private const val BASE_URL = "http://192.168.1.121:5000/"

    // Logging interceptor para debug (ver las peticiones/respuestas)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP con timeout y logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicio API
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}