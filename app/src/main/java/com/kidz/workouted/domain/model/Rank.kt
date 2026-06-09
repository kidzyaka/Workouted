package com.kidz.workouted.domain.model

import androidx.compose.ui.graphics.Color
import com.kidz.workouted.ui.theme.*

enum class Rank(val minScore: Int, val color: Color) {
    TREE(0, RankTree),
    BRONZE(100, RankBronze),
    SILVER(150, RankSilver),
    GOLD(200, RankGold),
    PLATINUM(300, RankPlatinum),
    EMERALD(400, RankEmerald);

    companion object {
        fun fromScore(score: Int): Rank {
            return entries.lastOrNull { score >= it.minScore } ?: TREE
        }
    }
}
