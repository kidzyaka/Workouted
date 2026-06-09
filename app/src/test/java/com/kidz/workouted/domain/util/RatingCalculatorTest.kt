package com.kidz.workouted.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RatingCalculatorTest {

    @Test
    fun `calculateBaseEffort returns correct value`() {
        // E = (W * (1 + R / 30) / W_max) * (H / 175) * S
        // W = 100, R = 10, W_max = 200, H = 175, S = 1000
        // E = (100 * (1 + 10 / 30.0) / 200) * (175 / 175) * 1000
        // E = (100 * 1.3333 / 200) * 1 * 1000
        // E = 0.6666 * 1000 = 666.666
        
        val result = RatingCalculator.calculateBaseEffort(
            weight = 100.0,
            reps = 10,
            maxWeightReference = 200.0,
            userHeightCm = 175.0
        )
        
        assertEquals(666.66, result, 0.01)
    }

    @Test
    fun `calculateBaseEffort with height correction`() {
        // H = 180
        // E = (100 * 1.3333 / 200) * (180 / 175) * 1000
        // E = 0.6666 * 1.02857 * 1000 = 685.71
        
        val result = RatingCalculator.calculateBaseEffort(
            weight = 100.0,
            reps = 10,
            maxWeightReference = 200.0,
            userHeightCm = 180.0
        )
        
        assertEquals(685.71, result, 0.01)
    }

    @Test
    fun `calculateMuscleRating returns correct value`() {
        val baseEffort = 1000.0
        val impactCoefficient = 0.5
        val result = RatingCalculator.calculateMuscleRating(baseEffort, impactCoefficient)
        
        assertEquals(500.0, result, 0.0)
    }
}
