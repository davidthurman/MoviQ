package com.dthurman.moviesaver.feature_movies.domain.util

import androidx.annotation.StringRes
import com.dthurman.moviesaver.R

/**
 * Defines the different ways movies can be ordered/sorted.
 */
enum class MovieOrder(@StringRes val displayNameRes: Int) {
    TITLE_ASC(R.string.sort_title_asc),
    TITLE_DESC(R.string.sort_title_desc),
    DATE_ADDED_ASC(R.string.sort_date_added_asc),
    DATE_ADDED_DESC(R.string.sort_date_added_desc),
    RELEASE_DATE_ASC(R.string.sort_release_date_asc),
    RELEASE_DATE_DESC(R.string.sort_release_date_desc),
    RATING_ASC(R.string.sort_rating_asc),
    RATING_DESC(R.string.sort_rating_desc)
}