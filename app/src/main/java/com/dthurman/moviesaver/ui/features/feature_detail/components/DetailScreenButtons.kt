package com.dthurman.moviesaver.ui.features.feature_detail.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFilled: Boolean = true,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    iconContentDescription: String? = null
) {
    if (isFilled) {
        Button(
            modifier = modifier,
            onClick = onClick,
        ) {
            startIcon?.let {
                Icon(it, iconContentDescription)
                Spacer(Modifier.width(8.dp))
            }
            Text(text)
            endIcon?.let {
                Spacer(Modifier.width(8.dp))
                Icon(it, iconContentDescription)
            }
        }
    } else {
        OutlinedButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            startIcon?.let {
                Icon(it, iconContentDescription)
                Spacer(Modifier.width(8.dp))
            }
            Text(text)
            endIcon?.let {
                Spacer(Modifier.width(8.dp))
                Icon(it, iconContentDescription)
            }
        }
    }
}