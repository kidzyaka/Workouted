package com.kidz.workouted.domain.util

import kotlin.math.roundToInt

object RatingCalculator {
    private const val REFERENCE_HEIGHT = 175.0
    private const val MULTIPLIER = 1000.0

    /**
     * Calculates the Base Effort (E) for a single set.
     * E = (W * (1 + R / 30) / W_max) * (H / 175) * S
     */
    fun calculateBaseEffort(
        weight: Double,
        reps: Int,
        maxWeightReference: Double,
        userHeightCm: Double
    ): Double {
        if (maxWeightReference <= 0) return 0.0
        
        val oneRepMaxEstimate = weight * (1.0 + reps / 30.0)
        val relativeStrength = oneRepMaxEstimate / maxWeightReference
        val heightCorrection = userHeightCm / REFERENCE_HEIGHT
        
        return relativeStrength * heightCorrection * MULTIPLIER
    }

    /**
     * Calculates the rating for a specific muscle based on base effort and impact coefficient.
     * P_i = E * C_i
     */
    fun calculateMuscleRating(baseEffort: Double, impactCoefficient: Double): Double {
        return baseEffort * impactCoefficient
    }
}
