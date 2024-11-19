package com.example.proyecto

data class MisAlimento(
    val name: String = "",
    val image: String = "",
    val info_nutricion: Map<String, String> = emptyMap() // Proteinas, calorias, grasas, carbohidratos
)
 {
    constructor() : this("", "", emptyMap())
}
