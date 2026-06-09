package com.kidz.workouted.domain.usecase

import javax.inject.Inject

/**
 * Calculates the estimated One Rep Max (1RM) using the Epley formula.
 * Formula: 1RM = W * (1 + R / 30)
 */
class CalculateOneRepMaxUseCase @Inject constructor() {
    operator fun invoke(weight: Double, reps: Int): Double {
        if (reps <= 0) return weight
        if (reps == 1) return weight
        return weight * (1.0 + reps / 30.0)
    }
}
