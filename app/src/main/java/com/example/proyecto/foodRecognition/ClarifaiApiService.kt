import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class InputData(
    val user_app_id: UserAppId,
    val inputs: List<Input>
)

data class UserAppId(
    val user_id: String,
    val app_id: String
)

data class Input(
    val data: Data
)

data class Data(
    val image: Image
)

data class Image(
    val url: String? = null,        // Para imágenes de URL
    val base64: String? = null      // Para imágenes en base64
)


data class PredictionResponse(
    val outputs: List<Output>
)

data class Output(
    val data: ConceptData
)

data class ConceptData(
    val concepts: List<Concept>
)

data class Concept(
    val id: String,
    val name: String,
    val value: Double
)

interface ClarifaiApiService {

    @Headers(
        "Authorization: Key 9b6945f895f3491fa88d054d530f5075", // Reemplaza con tu token
        "Content-Type: application/json"
    )
    @POST("v2/models/food-item-recognition/outputs")
    suspend fun predictFood(@Body requestBody: InputData): PredictionResponse
}

