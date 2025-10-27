package com.dthurman.moviesaver.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button (
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = text)
    }
}