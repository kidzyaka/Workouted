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
        val cursor = db.query("SELECT COUNT(*) FROM exercises")
        if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            if (count == 0) {
                seedData(db)
            }
        }
        cursor.close()
    }

    private fun seedData(db: SupportSQLiteDatabase) {
        Log.d("DatabaseSeeding", "Seeding database...")
        
        // 1. Muscle Groups
        val groups = listOf(
            "group_chest" to "Грудь",
            "group_back" to "Спина",
            "group_legs" to "Ноги",
            "group_shoulders" to "Плечи",
            "group_arms" to "Руки",
            "group_core" to "Кор"
        )
        
        groups.forEach { (id, name) ->
            val values = ContentValues().apply {
                put("id", id)
                put("name", name)
            }
            db.insert("muscle_groups", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        // 2. Muscles
        val muscles = listOf(
            arrayOf("m_chest_mid", "group_chest", "Большая грудная", 0.75),
            arrayOf("m_chest_upper", "group_chest", "Верхняя часть груди", 0.25),
            arrayOf("m_lats", "group_back", "Широчайшие", 0.45),
            arrayOf("m_traps", "group_back", "Трапеции", 0.20),
            arrayOf("m_lower_back", "group_back", "Поясница", 0.20),
            arrayOf("m_rhomboids", "group_back", "Ромбовидные", 0.15),
            arrayOf("m_quads", "group_legs", "Квадрицепсы", 0.40),
            arrayOf("m_glutes", "group_legs", "Ягодичные", 0.30),
            arrayOf("m_hamstrings", "group_legs", "Бицепс бедра", 0.20),
            arrayOf("m_calves", "group_legs", "Икроножные", 0.10),
            arrayOf("m_delt_lateral", "group_shoulders", "Средняя дельта", 0.40),
            arrayOf("m_delt_front", "group_shoulders", "Передняя дельта", 0.30),
            arrayOf("m_delt_rear", "group_shoulders", "Задняя дельта", 0.30),
            arrayOf("m_triceps", "group_arms", "Трицепс", 0.55),
            arrayOf("m_biceps", "group_arms", "Бицепс", 0.35),
            arrayOf("m_forearms", "group_arms", "Предплечья", 0.10),
            arrayOf("m_abs", "group_core", "Прямая мышца живота", 0.70),
            arrayOf("m_obliques", "group_core", "Косые мышцы", 0.30)
        )

        for (m in muscles) {
            val values = ContentValues().apply {
                put("id", m[0] as String)
                put("groupId", m[1] as String)
                put("name", m[2] as String)
                put("anatomicalWeight", m[3] as Double)
            }
            db.insert("muscles", SQLiteDatabase.CONFLICT_REPLACE, values)
        }

        // 3. Default Exercises & 4. Impacts from exercises_db.md
        val exercisesData = listOf(
            ExerciseData("Жим штанги лежа (классический)", 120.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.5)),
            ExerciseData("Жим штанги на наклонной скамье", 100.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.7, "m_triceps" to 0.4)),
            ExerciseData("Жим гантелей лежа", 100.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.5, "m_triceps" to 0.4)),
            ExerciseData("Жим гантелей на наклонной скамье", 80.0, listOf("m_chest_upper" to 1.0, "m_delt_front" to 0.6, "m_triceps" to 0.3)),
            ExerciseData("Разводка гантелей лежа", 40.0, listOf("m_chest_mid" to 1.0, "m_delt_front" to 0.3)),
            ExerciseData("Сведение рук в кроссовере", 50.0, listOf("m_chest_mid" to 1.0, "m_chest_upper" to 0.4)),
            ExerciseData("Отжимания от пола (с доп. весом)", 100.0, listOf("m_chest_mid" to 0.8, "m_triceps" to 0.6, "m_delt_front" to 0.4, "m_abs" to 0.3)),
            ExerciseData("Приседания со штангой на плечах", 150.0, listOf("m_quads" to 1.0, "m_glutes" to 0.8, "m_hamstrings" to 0.4, "m_lower_back" to 0.3, "m_abs" to 0.2)),
            ExerciseData("Фронтальные приседания", 120.0, listOf("m_quads" to 1.0, "m_glutes" to 0.5, "m_lower_back" to 0.4, "m_abs" to 0.4)),
            ExerciseData("Гакк-приседания", 180.0, listOf("m_quads" to 1.0, "m_glutes" to 0.4)),
            ExerciseData("Становая тяга (классическая)", 180.0, listOf("m_glutes" to 1.0, "m_lower_back" to 1.0, "m_hamstrings" to 0.9, "m_traps" to 0.6, "m_forearms" to 0.5, "m_quads" to 0.4, "m_lats" to 0.4)),
            ExerciseData("Становая тяга сумо", 190.0, listOf("m_glutes" to 1.0, "m_quads" to 0.8, "m_lower_back" to 0.8, "m_hamstrings" to 0.7, "m_traps" to 0.5, "m_forearms" to 0.5)),
            ExerciseData("Румынская тяга", 130.0, listOf("m_hamstrings" to 1.0, "m_glutes" to 0.8, "m_lower_back" to 0.6, "m_forearms" to 0.4)),
            ExerciseData("Жим ногами в тренажере", 300.0, listOf("m_quads" to 1.0, "m_glutes" to 0.6, "m_hamstrings" to 0.2)),
            ExerciseData("Выпады с гантелями", 90.0, listOf("m_quads" to 1.0, "m_glutes" to 0.9, "m_hamstrings" to 0.3)),
            ExerciseData("Болгарские сплит-приседания", 80.0, listOf("m_quads" to 1.0, "m_glutes" to 0.9, "m_hamstrings" to 0.3)),
            ExerciseData("Сгибание ног в тренажере (изоляция)", 75.0, listOf("m_hamstrings" to 1.0, "m_calves" to 0.2)),
            ExerciseData("Разгибание ног в тренажере (изоляция)", 95.0, listOf("m_quads" to 1.0)),
            ExerciseData("Подъем на носки стоя", 150.0, listOf("m_calves" to 1.0)),
            ExerciseData("Подъем на носки сидя", 80.0, listOf("m_calves" to 1.0)),
            ExerciseData("Подтягивания (с дополнительным весом)", 110.0, listOf("m_lats" to 1.0, "m_biceps" to 0.6, "m_rhomboids" to 0.5, "m_forearms" to 0.4)),
            ExerciseData("Подтягивания обратным хватом", 110.0, listOf("m_lats" to 0.9, "m_biceps" to 0.9, "m_rhomboids" to 0.4, "m_forearms" to 0.4)),
            ExerciseData("Тяга штанги в наклоне", 100.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.8, "m_traps" to 0.6, "m_biceps" to 0.5, "m_delt_rear" to 0.5, "m_lower_back" to 0.4)),
            ExerciseData("Тяга гантели в наклоне одной рукой", 60.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.7, "m_biceps" to 0.4)),
            ExerciseData("Тяга Т-грифа", 110.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.9, "m_traps" to 0.7, "m_biceps" to 0.5, "m_lower_back" to 0.3)),
            ExerciseData("Тяга верхнего блока к груди", 100.0, listOf("m_lats" to 1.0, "m_biceps" to 0.5, "m_rhomboids" to 0.4)),
            ExerciseData("Тяга нижнего блока к поясу", 110.0, listOf("m_lats" to 1.0, "m_rhomboids" to 0.9, "m_traps" to 0.5, "m_biceps" to 0.4, "m_lower_back" to 0.3)),
            ExerciseData("Пуловер с гантелей", 45.0, listOf("m_lats" to 1.0, "m_chest_mid" to 0.5, "m_triceps" to 0.3)),
            ExerciseData("Армейский жим стоя", 75.0, listOf("m_delt_front" to 1.0, "m_triceps" to 0.7, "m_delt_lateral" to 0.6, "m_abs" to 0.3)),
            ExerciseData("Жим гантелей сидя", 70.0, listOf("m_delt_front" to 1.0, "m_delt_lateral" to 0.7, "m_triceps" to 0.6)),
            ExerciseData("Махи гантелями в стороны", 22.0, listOf("m_delt_lateral" to 1.0, "m_traps" to 0.3)),
            ExerciseData("Махи гантелями перед собой", 20.0, listOf("m_delt_front" to 1.0)),
            ExerciseData("Махи гантелями в наклоне", 24.0, listOf("m_delt_rear" to 1.0, "m_rhomboids" to 0.4, "m_traps" to 0.3)),
            ExerciseData("Отжимания на брусьях (с отягощением)", 130.0, listOf("m_triceps" to 1.0, "m_chest_mid" to 0.8, "m_delt_front" to 0.6)),
            ExerciseData("Французский жим лежа", 50.0, listOf("m_triceps" to 1.0)),
            ExerciseData("Разгибание рук на верхнем блоке", 45.0, listOf("m_triceps" to 1.0)),
            ExerciseData("Разгибание рук с гантелью из-за головы", 40.0, listOf("m_triceps" to 1.0)),
            ExerciseData("Подъем штанги на бицепс стоя", 55.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.4)),
            ExerciseData("Подъем гантелей на бицепс с супинацией", 45.0, listOf("m_biceps" to 1.0, "m_forearms" to 0.3)),
            ExerciseData("Сгибания рук на скамье Скотта", 40.0, listOf("m_biceps" to 1.0)),
            ExerciseData("Упражнение \"Молотки\" с гантелями", 50.0, listOf("m_biceps" to 0.8, "m_forearms" to 1.0)),
            ExerciseData("Сгибание кистей со штангой", 45.0, listOf("m_forearms" to 1.0)),
            ExerciseData("Шраги со штангой", 140.0, listOf("m_traps" to 1.0, "m_forearms" to 0.5)),
            ExerciseData("Шраги с гантелями", 120.0, listOf("m_traps" to 1.0, "m_forearms" to 0.6)),
            ExerciseData("Скручивания на полу (с отягощением)", 40.0, listOf("m_abs" to 1.0)),
            ExerciseData("Скручивания на верхнем блоке (Молитва)", 80.0, listOf("m_abs" to 1.0, "m_obliques" to 0.2)),
            ExerciseData("Подъем ног в висе", 20.0, listOf("m_abs" to 1.0)),
            ExerciseData("Русские скручивания (с весом)", 30.0, listOf("m_obliques" to 1.0, "m_abs" to 0.6)),
            ExerciseData("Гиперэкстензия (с весом)", 40.0, listOf("m_lower_back" to 1.0, "m_glutes" to 0.5, "m_hamstrings" to 0.4)),
            ExerciseData("Ягодичный мостик со штангой", 160.0, listOf("m_glutes" to 1.0, "m_hamstrings" to 0.4, "m_lower_back" to 0.2))
        )

        exercisesData.forEachIndexed { index, data ->
            val exValues = ContentValues().apply {
                put("id", index + 1)
                put("name", data.name)
                put("isWeightBased", 1)
                put("maxWeightReference", data.wMax)
            }
            db.insert("exercises", SQLiteDatabase.CONFLICT_REPLACE, exValues)
            
            data.impacts.forEach { (muscleId, coef) ->
                val impValues = ContentValues().apply {
                    put("exerciseId", index + 1)
                    put("muscleId", muscleId)
                    put("impactCoefficient", coef)
                }
                db.insert("muscle_impacts", SQLiteDatabase.CONFLICT_REPLACE, impValues)
            }
        }
    }

    private data class ExerciseData(
        val name: String,
        val wMax: Double,
        val impacts: List<Pair<String, Double>>
    )
}
