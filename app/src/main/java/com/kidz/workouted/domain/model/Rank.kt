package com.kidz.workouted.domain.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.kidz.workouted.R
import com.kidz.workouted.ui.theme.*

/**
 * Represents the progression rank for a muscle group.
 * Thresholds are calibrated for a multiplier S = 500 in the effort formula.
 */
enum class Rank(val minScore: Int, val color: Color, @StringRes val nameRes: Int) {
    WOOD(0, RankTree, R.string.rank_wood),
    BRONZE(60, RankBronze, R.string.rank_bronze),
    SILVER(110, RankSilver, R.string.rank_silver),
    GOLD(170, RankGold, R.string.rank_gold),
    PLATINUM(240, RankPlatinum, R.string.rank_platinum),
    EMERALD(320, RankEmerald, R.string.rank_emerald),
    DIAMOND(420, RankDiamond, R.string.rank_diamond),
    ELITE(550, RankElite, R.string.rank_elite);

    companion object {
        /**
         * Returns the highest rank achieved for a given score.
         * Implementation follows a linear search from highest threshold to lowest.
         */
        fun fromScore(score: Int): Rank {
            return entries
                .sortedByDescending { it.minScore }
                .firstOrNull { score >= it.minScore } ?: WOOD
        }
    }
}
