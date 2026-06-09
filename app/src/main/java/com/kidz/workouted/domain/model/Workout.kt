package com.kidz.workouted.domain.model

data class Workout(
    val id: Long = 0,
    val timestamp: Long,
    val totalVolume: Double = 0.0,
    val exercisesCount: Int = 0
)
