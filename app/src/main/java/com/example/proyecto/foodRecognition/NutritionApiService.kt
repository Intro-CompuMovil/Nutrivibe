import retrofit2.http.GET
import retrofit2.http.Query

interface NutritionApiService {
    @GET("foods/search")
    suspend fun getFoodInfo(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "s5fzVyrU31cCFsWjP1XhJZ6Bqlg93wlCca17XBPc"
    ): NutritionResponse
}

// Clases para los datos de la respuesta
data class NutritionResponse(
    val foods: List<Food> // List of foods found
)

data class Food(
    val description: String, // Name or description of the food
    val foodNutrients: List<FoodNutrient> // Nutrient details
)

data class FoodNutrient(
    val nutrientName: String, // Name of the nutrient
    val value: Double, // Quantity of the nutrient
    val unitName: String // Unit of measurement for the nutrient
)
