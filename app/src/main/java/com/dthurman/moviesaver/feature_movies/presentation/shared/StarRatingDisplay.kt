package com.dthurman.moviesaver.feature_movies.presentation.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.ui.theme.extendedColors

@Composable
fun StarRatingDisplay(
    rating: Float?,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp,
    spacing: Dp = 4.dp,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
    ) {
        for (i in 1..5) {
            val isSelected = rating != null && i <= rating
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = stringResource(R.string.star_content_description, i),
                tint = if (isSelected) MaterialTheme.extendedColors.starRating else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

