package com.kidz.workouted.presentation.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    itemHeight: Dp = 48.dp,
    visibleItemsCount: Int = 3,
    onItemSelected: (Int) -> Unit
) {
    val lazyListState = rememberLazyListState(initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState)
    
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val haptic = LocalHapticFeedback.current

    val currentIndex by remember {
        derivedStateOf {
            val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset
            
            if (firstVisibleItemScrollOffset > itemHeightPx / 2) {
                (firstVisibleItemIndex + 1).coerceAtMost(items.size - 1)
            } else {
                firstVisibleItemIndex
            }
        }
    }

    LaunchedEffect(currentIndex) {
        if (lazyListState.isScrollInProgress) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    var lastEmittedIndex by remember { mutableIntStateOf(initialIndex) }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            if (currentIndex != lastEmittedIndex) {
                lastEmittedIndex = currentIndex
                onItemSelected(currentIndex)
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Selection Highlight
        Box(
            modifier = Modifier
                .height(itemHeight)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Optional: Draw lines or background for selection
        }

        LazyColumn(
            state = lazyListState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            fontSize = if (isSelected) 24.sp else 20.sp
                        )
                    )
                }
            }
        }
    }
}
