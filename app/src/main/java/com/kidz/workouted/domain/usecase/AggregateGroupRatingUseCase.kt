package com.kidz.workouted.domain.usecase

import javax.inject.Inject

/**
 * Calculates weighted average rating for a muscle group.
 * Formula: R_group = Sum(R_i * A_wi) / Sum(A_wi)
 * Where R_i is muscle rating and A_wi is its anatomical weight.
 * Note: If Sum(A_wi) is used as denominator, it's a true weighted average. 
 * The task says "weighted average of anatomical weights", so we multiply by weight.
 *
 * @param muscleRatings: Map of muscle ID to its current rating (R_i)
 * @param anatomicalWeights: Map of muscle ID to its anatomical weight (A_wi)
 */
class AggregateGroupRatingUseCase @Inject constructor() {
    operator fun invoke(
        muscleRatings: Map<Long, Double>,
        anatomicalWeights: Map<Long, Double>
    ): Double {
        var totalWeightedRating = 0.0
        var totalAnatomicalWeight = 0.0
        
        muscleRatings.forEach { (muscleId, rating) ->
            val weight = anatomicalWeights[muscleId] ?: 0.0
            totalWeightedRating += rating * weight
            totalAnatomicalWeight += weight
        }
        
        return if (totalAnatomicalWeight > 0) {
            totalWeightedRating / totalAnatomicalWeight
        } else {
            0.0
        }
    }
}
