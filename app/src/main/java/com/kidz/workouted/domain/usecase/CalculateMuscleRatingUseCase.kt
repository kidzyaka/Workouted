package com.kidz.workouted.domain.usecase

import javax.inject.Inject

/**
 * Calculates final rating points for a specific muscle.
 * Formula: P_i = E * C_i
 *
 * @param baseEffort (E): Base effort from the set
 * @param impactCoefficient (C_i): Muscle impact coefficient (0.1 to 1.0)
 */
class CalculateMuscleRatingUseCase @Inject constructor() {
    operator fun invoke(baseEffort: Double, impactCoefficient: Double): Double {
        return baseEffort * impactCoefficient
    }
}
