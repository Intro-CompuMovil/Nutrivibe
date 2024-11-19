package com.example.proyecto.foodRecognition

import Food
import NutritionApiService

class NutritionRepository(private val apiService: NutritionApiService) {
    suspend fun getNutritionalInfo(foodName: String): Food? {
        return try {
            val response = apiService.getFoodInfo(query = foodName)
            response.foods.firstOrNull() // Return the first food result if available
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if an error occurs
        }
    }
}
