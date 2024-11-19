import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.clarifai.com/"

    // Configuración del interceptor para registro detallado
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configuración del cliente HTTP con tiempos de espera personalizados y limpieza de conexiones inactivas
    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES)) // Mantener conexiones inactivas hasta 5 minutos
        .connectTimeout(30, TimeUnit.SECONDS) // Tiempo de espera para establecer conexión
        .readTimeout(30, TimeUnit.SECONDS)    // Tiempo de espera para leer datos
        .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo de espera para escribir datos
        .addInterceptor(logging) // Agrega el interceptor de logging
        .build()

    // Configuración de Retrofit
    val apiService: ClarifaiApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ClarifaiApiService::class.java)
}
