package com.kidz.workouted.domain.usecase

import javax.inject.Inject

/**
 * Calculates the Base Effort (E) for a single set.
 * Formula: E = (W * (1 + R / 30) / W_max) * (H / 175) * 550
 *
 * @param weight (W): Working weight
 * @param reps (R): Repetitions
 * @param maxWeightReference (W_max): Reference maximum weight for the exercise
 * @param userHeightCm (H): User's height in cm
 */
class CalculateSetEffortUseCase @Inject constructor() {
    operator fun invoke(
        weight: Double,
        reps: Int,
        maxWeightReference: Double,
        userHeightCm: Double
    ): Double {
        if (maxWeightReference <= 0) return 0.0
        
        val oneRepMaxEstimate = weight * (1.0 + reps / 30.0)
        val relativeStrength = oneRepMaxEstimate / maxWeightReference
        val heightCorrection = (userHeightCm / 175.0).coerceAtMost(1.03)
        
        return relativeStrength * heightCorrection * 400.0
    }
}
