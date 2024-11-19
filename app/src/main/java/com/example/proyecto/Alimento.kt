package com.example.proyecto

import java.io.Serializable

data class Alimento<T>(
    val name: String = "",
    val image: String = "",
    val info_nutricion: Map<String, T> = emptyMap(),
    val supermarkets: List<Supermarket> = emptyList()
) : Serializable {
    constructor() : this("", "", emptyMap(), emptyList())
}
data class Supermarket( // Represents each supermarket object in the JSON
    val name: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0
) : Serializable {
    constructor() : this("", 0.0, 0.0)
}
