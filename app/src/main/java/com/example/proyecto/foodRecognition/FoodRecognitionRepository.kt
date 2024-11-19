import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class FoodRecognitionRepository(private val apiService: ClarifaiApiService) {

    // Método para predecir desde una URL
    suspend fun predictFood(imageUrl: String): List<Concept>? {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = InputData(
                    user_app_id = UserAppId(
                        user_id = "rgf5sx4hv6ki",      // Tu user_id
                        app_id = "food-recognition-app" // Tu app_id
                    ),
                    inputs = listOf(
                        Input(
                            data = Data(
                                image = Image(url = imageUrl) // Envía una URL
                            )
                        )
                    )
                )

                val response = apiService.predictFood(requestBody)
                response.outputs.firstOrNull()?.data?.concepts
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                println("Error: Tiempo de espera agotado al intentar conectar con la API.")
                null
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error inesperado al procesar la solicitud: ${e.message}")
                null
            }
        }
    }

    // Método para predecir desde una imagen en base64
    suspend fun predictFoodFromBase64(base64Image: String): List<Concept>? {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = InputData(
                    user_app_id = UserAppId(
                        user_id = "rgf5sx4hv6ki",      // Tu user_id
                        app_id = "food-recognition-app" // Tu app_id
                    ),
                    inputs = listOf(
                        Input(
                            data = Data(
                                image = Image(base64 = base64Image) // Envía base64
                            )
                        )
                    )
                )

                val response = apiService.predictFood(requestBody)
                response.outputs.firstOrNull()?.data?.concepts
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                println("Error: Tiempo de espera agotado al intentar conectar con la API.")
                null
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error inesperado al procesar la solicitud: ${e.message}")
                null
            }
        }
    }
}
