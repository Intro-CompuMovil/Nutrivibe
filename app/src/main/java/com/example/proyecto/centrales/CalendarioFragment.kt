package com.example.proyecto.centrales

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.ExtrasActivity
import com.example.proyecto.PlatosAdapter
import com.example.proyecto.Plato
import com.example.proyecto.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var recyclerViewDesayuno: RecyclerView
    private lateinit var recyclerViewAlmuerzo: RecyclerView
    private lateinit var recyclerViewCena: RecyclerView

    private lateinit var databaseReference: DatabaseReference
    private lateinit var plansReference: DatabaseReference
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)

        databaseReference = FirebaseDatabase.getInstance().getReference("platos")
        plansReference = FirebaseDatabase.getInstance().getReference("Plans")
        val btnViewExtras = view.findViewById<Button>(R.id.btnViewExtras)
        btnViewExtras.setOnClickListener {
            val selectedDate = tvSelectedDate.text.toString() // Use the displayed date
            val intent = Intent(requireContext(), ExtrasActivity::class.java)
            intent.putExtra("selectedDate", selectedDate) // Pass the selected date
            startActivity(intent)
        }


        setupInitialDate()
        setupRecyclerViews()
    }

    private fun initializeViews(view: View) {
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        val btnSelectDate = view.findViewById<MaterialButton>(R.id.btnSelectDate)

        recyclerViewDesayuno = view.findViewById(R.id.recyclerViewDesayuno)
        recyclerViewAlmuerzo = view.findViewById(R.id.recyclerViewAlimentosAlmuerzo)
        recyclerViewCena = view.findViewById(R.id.recyclerViewCena)

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupInitialDate() {
        val today = Calendar.getInstance()
        updateDateDisplay(today)
        verifyAndFetchPlans(today)
    }

    private fun updateDateDisplay(calendar: Calendar) {
        val formattedDate = dateFormatter.format(calendar.time)
        tvSelectedDate.text = formattedDate
    }

    private fun setupRecyclerViews() {
        recyclerViewDesayuno.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAlmuerzo.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCena.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                updateDateDisplay(selectedCalendar)
                fetchPlatesForDate(selectedCalendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun verifyAndFetchPlans(today: Calendar) {
        if (userId == null) {
            Log.e("CalendarioFragment", "User is not authenticated.")
            return
        }

        plansReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("CalendarioFragment", "User's plans exist in the database.")
                } else {
                    Log.d("CalendarioFragment", "User's plans do not exist. Creating default structure.")
                    plansReference.child(userId).setValue(mapOf<String, Any>())
                }

                checkTodayInPlans(today)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarioFragment", "Error verifying user's plans: ${error.message}")
            }
        })
    }

    private fun checkTodayInPlans(today: Calendar) {
        val todayDate = dateFormatter.format(today.time)

        plansReference.child(userId!!).child(todayDate).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("CalendarioFragment", "Plans already exist for today: $todayDate")
                    fetchPlatesForDate(today)
                } else {
                    Log.d("CalendarioFragment", "No plans for today. Generating default meals.")
                    generateDefaultMealsForToday(todayDate)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarioFragment", "Error checking today's plans: ${error.message}")
            }
        })
    }

    private fun generateDefaultMealsForToday(date: String) {
        val categories = listOf("desayunos", "almuerzos", "cenas")
        val mealPlan = mutableMapOf<String, Plato>()

        Log.d("CalendarioFragment", "Fetching default meals for $date...")

        databaseReference.child("alimentos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("CalendarioFragment", "'alimentos' node exists in database.")
                    for (category in categories) {
                        var categoryFound = false

                        // Iterate over numeric indices in the database (e.g., 0, 1, 2, ...)
                        for (indexSnapshot in snapshot.children) {
                            val categorySnapshot = indexSnapshot.child(category.lowercase())
                            if (categorySnapshot.exists()) {
                                categoryFound = true
                                Log.d("CalendarioFragment", "Category '$category' found under index: ${indexSnapshot.key}")

                                // Load dishes under the category
                                val plates = categorySnapshot.children.mapNotNull { it.getValue(Plato::class.java) }
                                if (plates.isNotEmpty()) {
                                    val randomPlate = plates.random()
                                    mealPlan[category] = randomPlate
                                    Log.d("CalendarioFragment", "Generated random meal for '$category': ${randomPlate.nombre}")
                                } else {
                                    Log.e("CalendarioFragment", "No dishes available for '$category'. Adding a placeholder.")
                                    mealPlan[category] = Plato(
                                        nombre = "Default $category",
                                        imagen = "drawable/placeholder_image",
                                        tutorial = "",
                                        informacion_nutricional = mapOf("calorías" to "0")
                                    )
                                }
                            }
                        }

                        if (!categoryFound) {
                            Log.e("CalendarioFragment", "Category '$category' does not exist in database. Adding a placeholder.")
                            mealPlan[category] = Plato(
                                nombre = "Default $category",
                                imagen = "drawable/placeholder_image",
                                tutorial = "",
                                informacion_nutricional = mapOf("calorías" to "0")
                            )
                        }
                    }

                    // Save the meal plan to the user's plans
                    plansReference.child(userId!!).child(date).setValue(mealPlan)
                        .addOnSuccessListener {
                            Log.d("CalendarioFragment", "Default meals successfully saved for $date.")
                            fetchPlatesForDate(Calendar.getInstance()) // Refresh the UI
                        }
                        .addOnFailureListener { e ->
                            Log.e("CalendarioFragment", "Failed to save default meals for $date: ${e.message}")
                        }
                } else {
                    Log.e("CalendarioFragment", "'alimentos' node does not exist in database. Cannot generate meals.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarioFragment", "Error accessing 'alimentos' node: ${error.message}")
            }
        })
    }

    private fun fetchPlatesForDate(calendar: Calendar) {
        val selectedDate = dateFormatter.format(calendar.time)
        userId?.let { uid ->
            val userPlanReference = plansReference.child(uid).child(selectedDate)

            userPlanReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Plato>>() {}
                        val existingPlan: Map<String, Plato>? = snapshot.getValue(genericTypeIndicator)
                        if (existingPlan != null) {
                            displayExistingPlan(existingPlan)
                        } else {
                            Log.e("CalendarioFragment", "Plan data is null for date: $selectedDate")
                            showDefaultPlaceholders()
                        }
                    } else {
                        Log.e("CalendarioFragment", "No data for date: $selectedDate")
                        showDefaultPlaceholders()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CalendarioFragment", "Error fetching data for date $selectedDate: ${error.message}")
                }
            })
        } ?: Log.e("CalendarioFragment", "User is not authenticated.")
    }

    private fun displayExistingPlan(plan: Map<String, Plato>) {
        plan["desayunos"]?.let { displayPlates("desayunos", it) }
        plan["almuerzos"]?.let { displayPlates("almuerzos", it) }
        plan["cenas"]?.let { displayPlates("cenas", it) }
    }

    private fun showDefaultPlaceholders() {
        val defaultPlates = mapOf(
            "desayunos" to Plato(
                nombre = "No se ha añadido desayuno",
                imagen = "drawable/placeholder_image",
                tutorial = "",
                informacion_nutricional = mapOf("calorías" to "0")
            ),
            "almuerzos" to Plato(
                nombre = "No se ha añadido almuerzo",
                imagen = "drawable/placeholder_image",
                tutorial = "",
                informacion_nutricional = mapOf("calorías" to "0")
            ),
            "cenas" to Plato(
                nombre = "No se ha añadido cena",
                imagen = "drawable/placeholder_image",
                tutorial = "",
                informacion_nutricional = mapOf("calorías" to "0")
            )
        )

        recyclerViewDesayuno.adapter = PlatosAdapter(listOf(defaultPlates["desayunos"]!!)) {}
        recyclerViewAlmuerzo.adapter = PlatosAdapter(listOf(defaultPlates["almuerzos"]!!)) {}
        recyclerViewCena.adapter = PlatosAdapter(listOf(defaultPlates["cenas"]!!)) {}

        recyclerViewDesayuno.adapter?.notifyDataSetChanged()
        recyclerViewAlmuerzo.adapter?.notifyDataSetChanged()
        recyclerViewCena.adapter?.notifyDataSetChanged()

        Log.d("CalendarioFragment", "Default placeholders displayed.")
    }


    private fun displayPlates(category: String, plate: Plato) {
        when (category) {
            "desayunos" -> recyclerViewDesayuno.adapter = PlatosAdapter(listOf(plate)) {}
            "almuerzos" -> recyclerViewAlmuerzo.adapter = PlatosAdapter(listOf(plate)) {}
            "cenas" -> recyclerViewCena.adapter = PlatosAdapter(listOf(plate)) {}
        }
    }
}
