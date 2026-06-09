package com.kidz.workouted.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class UseCaseTests {

    @Test
    fun `CalculateSetEffortUseCase returns correct value`() {
        val useCase = CalculateSetEffortUseCase()
        // W = 100, R = 10, W_max = 200, H = 175
        // E = (100 * (1 + 10/30.0) / 200) * (175/175) * 100
        // E = (100 * 1.3333 / 200) * 1 * 100 = 66.666
        
        val result = useCase(
            weight = 100.0,
            reps = 10,
            maxWeightReference = 200.0,
            userHeightCm = 175.0
        )
        assertEquals(66.66, result, 0.01)
    }

    @Test
    fun `CalculateMuscleRatingUseCase returns correct value`() {
        val useCase = CalculateMuscleRatingUseCase()
        val result = useCase(baseEffort = 66.66, impactCoefficient = 0.5)
        assertEquals(33.33, result, 0.01)
    }

    @Test
    fun `AggregateGroupRatingUseCase returns correct weighted average`() {
        val useCase = AggregateGroupRatingUseCase()
        val ratings = mapOf(1L to 50.0, 2L to 100.0)
        val weights = mapOf(1L to 0.7, 2L to 0.3)
        
        // (50 * 0.7 + 100 * 0.3) / (0.7 + 0.3) = (35 + 30) / 1.0 = 65.0
        val result = useCase(ratings, weights)
        assertEquals(65.0, result, 0.01)
    }
}
