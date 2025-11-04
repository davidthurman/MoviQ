package com.dthurman.moviesaver.feature_movies.presentation.detail.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dthurman.moviesaver.R
import com.dthurman.moviesaver.core.domain.model.Movie

@Composable
fun RemoveFromSeenDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    currentMovie: Movie
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.remove_from_seen),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            val message = if (currentMovie.isFavorite) {
                stringResource(R.string.remove_movie_with_favorite_message, currentMovie.title)
            } else {
                stringResource(R.string.remove_movie_message, currentMovie.title)
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.remove))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

