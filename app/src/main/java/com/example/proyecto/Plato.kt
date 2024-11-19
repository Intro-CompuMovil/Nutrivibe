package com.example.proyecto

import java.io.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plato(
    val nombre: String = "",
    val imagen: String = "",
    val tutorial: String = "",
    val informacion_nutricional: Map<String, String> = emptyMap(), // Cambiar a camelCase
    var extras: List<Alimento<String>> = emptyList() // List of additional Alimentos

) : Parcelable {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", emptyMap())
}

data class CategoriaPlatos(
    val snacks: List<Plato> = emptyList(),
    val desayunos: List<Plato> = emptyList(),
    val almuerzos: List<Plato> = emptyList(),
    val cenas: List<Plato> = emptyList()
) : Serializable {
    constructor() : this(emptyList(), emptyList(), emptyList(), emptyList())
}
