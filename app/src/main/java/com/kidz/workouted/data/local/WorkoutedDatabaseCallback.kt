package com.kidz.workouted.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

class WorkoutedDatabaseCallback @Inject constructor() : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        seedData(db)
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Ensure technical references are updated on each app open/update
        seedData(db)
    }

    private fun seedData(db: SupportSQLiteDatabase) {
        Log.d("DatabaseSeeding", "Updating technical references...")
        db.beginTransaction()
        try {
            // 1. Muscle Groups
            val groups = listOf(
                "group_chest" to "group_chest",
                "group_back" to "group_back",
                "group_legs" to "group_legs",
                "group_shoulders" to "group_shoulders",
                "group_arms" to "group_arms",
                "group_core" to "group_core"
            )
            
            groups.forEach { (id, nameKey) ->
                val values = ContentValues().apply {
                    put("id", id)
                    put("name", nameKey)
                }
                db.insert("muscle_groups", SQLiteDatabase.CONFLICT_IGNORE, values)
                db.update("muscle_groups", SQLiteDatabase.CONFLICT_NONE, values, "id = ?", arrayOf(id))
            }

            // 2. Muscles
            val muscles = listOf(
                arrayOf("m_chest_mid", "group_chest", "m_chest_mid", 0.75),
                arrayOf("m_chest_upper", "group_chest", "m_chest_upper", 0.25),
                arrayOf("m_lats", "group_back", "m_lats", 0.45),
                arrayOf("m_traps", "group_back", "m_traps", 0.20),
                arrayOf("m_lower_back", "group_back", "m_lower_back", 0.20),
                arrayOf("m_rhomboids", "group_back", "m_rhomboids", 0.15),
                arrayOf("m_quads", "group_legs", "m_quads", 0.40),
                arrayOf("m_glutes", "group_legs", "m_glutes", 0.30),
                arrayOf("m_hamstrings", "group_legs", "m_hamstrings", 0.20),
                arrayOf("m_calves", "group_legs", "m_calves", 0.10),
                arrayOf("m_delt_lateral", "group_shoulders", "m_delt_lateral", 0.40),
                arrayOf("m_delt_front", "group_shoulders", "m_delt_front", 0.30),
                arrayOf("m_delt_rear", "group_shoulders", "m_delt_rear", 0.30),
                arrayOf("m_triceps", "group_arms", "m_triceps", 0.45),
                arrayOf("m_biceps", "group_arms", "m_biceps", 0.40),
                arrayOf("m_forearms", "group_arms", "m_forearms", 0.15),
                arrayOf("m_abs", "group_core", "m_abs", 0.70),
                arrayOf("m_obliques", "group_core", "m_obliques", 0.30)
            )

            for (m in muscles) {
                val id = m[0] as String
                val values = ContentValues().apply {
                    put("id", id)
                    put("groupId", m[1] as String)
                    put("name", m[2] as String)
                    put("anatomicalWeight", m[3] as Double)
                }
                db.insert("muscles", SQLiteDatabase.CONFLICT_IGNORE, values)
                db.update("muscles", SQLiteDatabase.CONFLICT_NONE, values, "id = ?", arrayOf(id))
            }

            // 3. Exercises (References calibrated for Multiplier = 400)
            val exercisesData = listOf(
                ExerciseData("ex_bench_press_classic", 120.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.5)),
                ExerciseData("ex_incline_bench_press", 100.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.7, "m_triceps" to 0.4)),
                ExerciseData("ex_dumbbell_press", 100.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.5, "m_triceps" to 0.4)),
                ExerciseData("ex_incline_dumbbell_press", 80.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.3)),
                ExerciseData("ex_dumbbell_flyes", 40.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.3)),
                ExerciseData("ex_cable_crossover", 180.0, listOf("m_chest_mid" to 1.0, "m_chest_upper" to 0.4)),
                ExerciseData("ex_push_ups_weighted", 100.0, listOf("m_chest_mid" to 0.8, "m_triceps" to 0.6, "m_delt_front" to 0.4, "m_abs" to 0.3)),
                ExerciseData("ex_squats", 150.0, listOf("m_quads" to 1.0, "m_glutes" to 0.8, "m_hamstrings" to 0.4, "m_lower_back" to 0.3, "m_abs" to 0.2)),
                ExerciseData("ex_front_squats", 120.0, listOf("m_quads" to 1.0, "m_glutes" to 0.5, "m_lower_back" to 0.4, "m_abs" to 0.4)),
                ExerciseData("ex_hack_squats", 220.0, listOf("m_quads" to 1.0, "m_glutes" to 0.4)),
                ExerciseData("ex_deadlift_classic", 180.0, listOf("m_glutes" to 1.0, "m_lower_back" to 1.0, "m_hamstrings" to 0.9, "m_traps" to 0.6, "m_forearms" to 0.5, "m_quads" to 0.4, "m_lats" to 0.4)),
                ExerciseData("ex_deadlift_sumo", 190.0, listOf("m_glutes" to 1.0, "m_quads" to 0.8, "m_lower_back" to 0.8, "m_hamstrings" to 0.7, "m_traps" to 0.5, "m_forearms" to 0.5)),
                ExerciseData("ex_romanian_deadlift", 130.0, listOf("m_hamstrings" to 1.0, "m_glutes" to 0.8, "m_lower_back" to 0.6, "m_forearms" to 0.4)),
                ExerciseData("ex_leg_press", 800.0, listOf("m_quads" to 1.0, "m_glutes" to 0.6, "m_hamstrings" to 0.2)),
                ExerciseData("ex_dumbbell_lunges", 90.0, listOf("m_quads" to 1.0, "m_glutes" to 0.9, "m_hamstrings" to 0.3)),
                ExerciseData("ex_bulgarian_split_squat", 80.0, listOf("m_quads" to 1.0, "m_glutes" to 0.9, "m_hamstrings" to 0.3)),
                ExerciseData("ex_leg_curl", 240.0, listOf("m_hamstrings" to 1.0, "m_calves" to 0.2)),
                ExerciseData("ex_leg_extension", 260.0, listOf("m_quads" to 1.0)),
                ExerciseData("ex_calf_raise_standing", 150.0, listOf("m_calves" to 1.0)),
                ExerciseData("ex_calf_raise_sitting", 80.0, listOf("m_calves" to 1.0)),
                ExerciseData("ex_pull_ups_weighted", 110.0, listOf("m_lats" to 1.0, "m_biceps" to 0.6, "m_rhomboids" to 0.5, "m_forearms" to 0.4)),
                ExerciseData("ex_chin_ups", 110.0, listOf("m_lats" to 0.9, "m_biceps" to 0.9, "m_rhomboids" to 0.4, "m_forearms" to 0.4)),
                ExerciseData("ex_bent_over_row", 100.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.8, "m_traps" to 0.6, "m_biceps" to 0.5, "m_delt_rear" to 0.5, "m_lower_back" to 0.4)),
                ExerciseData("ex_one_arm_dumbbell_row", 60.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.7, "m_biceps" to 0.4)),
                ExerciseData("ex_t_bar_row", 110.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.9, "m_traps" to 0.7, "m_biceps" to 0.5, "m_lower_back" to 0.3)),
                ExerciseData("ex_lat_pulldown", 260.0, listOf("m_lats" to 1.0, "m_biceps" to 0.5, "m_rhomboids" to 0.4)),
                ExerciseData("ex_seated_cable_row", 260.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.9, "m_traps" to 0.5, "m_biceps" to 0.4, "m_lower_back" to 0.3)),
                ExerciseData("ex_dumbbell_pullover", 45.0, listOf("m_lats" to 1.0, "m_chest_mid" to 0.5, "m_triceps" to 0.3)),
                ExerciseData("ex_military_press", 75.0, listOf("m_delt_front" to 1.0, "m_triceps" to 0.7, "m_delt_lateral" to 0.6, "m_abs" to 0.3)),
                ExerciseData("ex_seated_dumbbell_press", 70.0, listOf("m_delt_front" to 1.0, "m_delt_lateral" to 0.7, "m_triceps" to 0.6)),
                ExerciseData("ex_lateral_raises", 22.0, listOf("m_delt_lateral" to 1.0, "m_traps" to 0.3)),
                ExerciseData("ex_front_raises", 20.0, listOf("m_delt_front" to 1.0)),
                ExerciseData("ex_reverse_flyes", 24.0, listOf("m_delt_rear" to 1.0, "m_rhomboids" to 0.4, "m_traps" to 0.3)),
                ExerciseData("ex_dips_weighted", 130.0, listOf("m_triceps" to 1.0, "m_chest_mid" to 0.8, "m_delt_front" to 0.6)),
                ExerciseData("ex_french_press", 50.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_tricep_extension_cable", 240.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_overhead_extension", 40.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_barbell_curl", 55.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.4)),
                ExerciseData("ex_dumbbell_curl", 45.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.3)),
                ExerciseData("ex_preacher_curl", 40.0, listOf("m_biceps" to 1.0)),
                ExerciseData("ex_hammer_curl", 50.0, listOf("m_biceps" to 0.8, "m_forearms" to 1.0)),
                ExerciseData("ex_wrist_curl", 45.0, listOf("m_forearms" to 1.0)),
                ExerciseData("ex_barbell_shrug", 140.0, listOf("m_traps" to 1.0, "m_forearms" to 0.5)),
                ExerciseData("ex_dumbbell_shrug", 120.0, listOf("m_traps" to 1.0, "m_forearms" to 0.6)),
                ExerciseData("ex_crunches_weighted", 40.0, listOf("m_abs" to 1.0)),
                ExerciseData("ex_cable_crunches", 150.0, listOf("m_abs" to 1.0, "m_obliques" to 0.2)),
                ExerciseData("ex_hanging_leg_raises", 20.0, listOf("m_abs" to 1.0)),
                ExerciseData("ex_russian_twist", 30.0, listOf("m_obliques" to 1.0, "m_abs" to 0.6)),
                ExerciseData("ex_hyperextension", 40.0, listOf("m_lower_back" to 1.0, "m_glutes" to 0.5, "m_hamstrings" to 0.4)),
                ExerciseData("ex_hip_thrust", 160.0, listOf("m_glutes" to 1.0, "m_hamstrings" to 0.4, "m_lower_back" to 0.2)),
                ExerciseData("ex_pec_deck", 260.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.3)),
                ExerciseData("ex_chest_press_machine", 280.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.5)),
                ExerciseData("ex_hammer_strength_chest_mid", 240.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.5, "m_triceps" to 0.4)),
                ExerciseData("ex_hammer_strength_chest_upper", 220.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.4)),
                ExerciseData("ex_smith_bench_press", 160.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.5)),
                ExerciseData("ex_smith_incline_press", 140.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.4)),
                ExerciseData("ex_cable_bicep_curl", 240.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.3)),
                ExerciseData("ex_low_pulley_bicep_curl", 260.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.4)),
                ExerciseData("ex_tricep_rope_pushdown", 250.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_tricep_straight_bar_pushdown", 260.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_bicep_curl_machine", 240.0, listOf("m_biceps" to 1.0)),
                ExerciseData("ex_tricep_extension_machine", 240.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_hip_abduction", 180.0, listOf("m_glutes" to 1.0)),
                ExerciseData("ex_hip_adduction", 180.0, listOf("m_quads" to 0.5, "m_glutes" to 0.5)),
                ExerciseData("ex_smith_squats", 250.0, listOf("m_quads" to 1.0, "m_glutes" to 0.7, "m_hamstrings" to 0.3)),
                ExerciseData("ex_smith_lunges", 180.0, listOf("m_quads" to 1.0, "m_glutes" to 0.9, "m_hamstrings" to 0.3)),
                ExerciseData("ex_smith_hip_thrust", 220.0, listOf("m_glutes" to 1.0, "m_hamstrings" to 0.4, "m_lower_back" to 0.2)),
                ExerciseData("ex_cable_kickback", 120.0, listOf("m_glutes" to 1.0, "m_hamstrings" to 0.3)),
                ExerciseData("ex_seated_leg_curl", 220.0, listOf("m_hamstrings" to 1.0, "m_calves" to 0.2)),
                ExerciseData("ex_single_leg_press", 250.0, listOf("m_quads" to 1.0, "m_glutes" to 0.6, "m_hamstrings" to 0.2)),
                ExerciseData("ex_calf_press_on_leg_press", 350.0, listOf("m_calves" to 1.0)),
                ExerciseData("ex_reverse_hack_squat", 220.0, listOf("m_glutes" to 1.0, "m_quads" to 0.8, "m_hamstrings" to 0.4)),
                ExerciseData("ex_smith_shoulder_press", 140.0, listOf("m_delt_front" to 1.0, "m_triceps" to 0.6, "m_delt_lateral" to 0.4)),
                ExerciseData("ex_hammer_shoulder_press", 150.0, listOf("m_delt_front" to 1.0, "m_triceps" to 0.6, "m_delt_lateral" to 0.4)),
                ExerciseData("ex_seated_row_cable", 240.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.9, "m_traps" to 0.5, "m_biceps" to 0.4, "m_lower_back" to 0.3)),
                ExerciseData("ex_cable_pullover", 160.0, listOf("m_lats" to 1.0, "m_chest_mid" to 0.5, "m_triceps" to 0.3)),
                ExerciseData("ex_reverse_peck_deck", 140.0, listOf("m_delt_rear" to 1.0, "m_rhomboids" to 0.4, "m_traps" to 0.3)),
                ExerciseData("ex_hammer_lat_pulldown", 260.0, listOf("m_lats" to 1.0, "m_biceps" to 0.5, "m_rhomboids" to 0.4)),
                ExerciseData("ex_hammer_seated_row", 260.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.8, "m_traps" to 0.6, "m_biceps" to 0.5)),
                ExerciseData("ex_close_grip_bench_press", 100.0, listOf("m_triceps" to 1.0, "m_chest_mid" to 0.7, "m_delt_front" to 0.5)),
                ExerciseData("ex_bench_dips", 60.0, listOf("m_triceps" to 1.0, "m_chest_mid" to 0.3, "m_delt_front" to 0.3)),
                ExerciseData("ex_one_arm_tricep_kickback", 20.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_seated_dumbbell_overhead_extension", 40.0, listOf("m_triceps" to 1.0)),
                ExerciseData("ex_reverse_barbell_curl", 45.0, listOf("m_forearms" to 1.0, "m_biceps" to 0.7)),
                ExerciseData("ex_ez_bar_curl", 60.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.3)),
                ExerciseData("ex_concentration_curl", 25.0, listOf("m_biceps" to 1.0)),
                ExerciseData("ex_high_cable_curl", 160.0, listOf("m_biceps" to 1.0)),
                ExerciseData("ex_upright_row_wide", 60.0, listOf("m_delt_lateral" to 1.0, "m_traps" to 0.6, "m_biceps" to 0.3)),
                ExerciseData("ex_arnold_press", 60.0, listOf("m_delt_front" to 1.0, "m_delt_lateral" to 0.7, "m_triceps" to 0.6)),
                ExerciseData("ex_face_pulls", 160.0, listOf("m_delt_rear" to 1.0, "m_rhomboids" to 0.8, "m_traps" to 0.6, "m_biceps" to 0.3)),
                ExerciseData("ex_one_arm_cable_lateral_raise", 60.0, listOf("m_delt_lateral" to 1.0)),
                ExerciseData("ex_cable_reverse_fly", 100.0, listOf("m_delt_rear" to 1.0, "m_rhomboids" to 0.5)),
                ExerciseData("ex_one_arm_low_pulley_row", 180.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.7, "m_biceps" to 0.4)),
                ExerciseData("ex_v_bar_pull_ups", 110.0, listOf("m_lats" to 1.0, "m_biceps" to 0.8, "m_forearms" to 0.5)),
                ExerciseData("ex_reverse_grip_lat_pulldown", 220.0, listOf("m_lats" to 1.0, "m_biceps" to 0.8, "m_rhomboids" to 0.4)),
                ExerciseData("ex_straight_arm_pulldown", 180.0, listOf("m_lats" to 1.0, "m_triceps" to 0.3)),
                ExerciseData("ex_smith_shrugs", 140.0, listOf("m_traps" to 1.0, "m_forearms" to 0.4)),
                ExerciseData("ex_weighted_step_ups", 60.0, listOf("m_quads" to 1.0, "m_glutes" to 0.8, "m_hamstrings" to 0.3)),
                ExerciseData("ex_zercher_squat", 120.0, listOf("m_quads" to 1.0, "m_glutes" to 0.6, "m_abs" to 0.7, "m_lower_back" to 0.5)),
                ExerciseData("ex_goblet_squat", 60.0, listOf("m_quads" to 1.0, "m_glutes" to 0.6, "m_abs" to 0.4)),
                ExerciseData("ex_single_leg_curl", 40.0, listOf("m_hamstrings" to 1.0, "m_calves" to 0.2)),
                ExerciseData("ex_sissy_squat_weighted", 40.0, listOf("m_quads" to 1.0, "m_abs" to 0.3)),
                ExerciseData("ex_ab_roller", 40.0, listOf("m_abs" to 1.0, "m_lats" to 0.4, "m_triceps" to 0.3)),
                ExerciseData("ex_bicycle_crunches", 20.0, listOf("m_obliques" to 1.0, "m_abs" to 0.8)),
                ExerciseData("ex_weighted_plank", 40.0, listOf("m_abs" to 1.0, "m_lower_back" to 0.6)),
                ExerciseData("ex_side_plank", 10.0, listOf("m_obliques" to 1.0, "m_abs" to 0.5)),
                ExerciseData("ex_captain_chair_leg_raises", 20.0, listOf("m_abs" to 1.0)),
                ExerciseData("ex_cable_woodchopper", 160.0, listOf("m_obliques" to 1.0, "m_abs" to 0.7, "m_delt_front" to 0.3)),
                ExerciseData("ex_swiss_ball_crunches", 30.0, listOf("m_abs" to 1.0))
            )

            exercisesData.forEachIndexed { index, data ->
                val exerciseId = (index + 1).toLong()
                val exValues = ContentValues().apply {
                    put("id", exerciseId)
                    put("name", data.name)
                    put("isWeightBased", 1)
                    put("maxWeightReference", data.wMax)
                }
                db.insert("exercises", SQLiteDatabase.CONFLICT_IGNORE, exValues)
                db.update("exercises", SQLiteDatabase.CONFLICT_NONE, exValues, "id = ?", arrayOf(exerciseId.toString()))
                
                // Clear old impacts for this exercise to prevent accumulation
                db.delete("muscle_impacts", "exerciseId = ?", arrayOf(exerciseId.toString()))
                
                data.impacts.forEach { (muscleId, coef) ->
                    val impValues = ContentValues().apply {
                        put("exerciseId", exerciseId)
                        put("muscleId", muscleId)
                        put("impactCoefficient", coef)
                    }
                    db.insert("muscle_impacts", SQLiteDatabase.CONFLICT_IGNORE, impValues)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private data class ExerciseData(
        val name: String,
        val wMax: Double,
        val impacts: List<Pair<String, Double>>
    )
}
