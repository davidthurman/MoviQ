package com.dthurman.moviesaver.core.util

object TestTags {

    // Seen Screen
    const val MY_MOVIES_SORT_BUTTON = "MY_MOVIES_SORT_BUTTON"
    const val MY_MOVIES_SORT_DROPDOWN_SECTION = "MY_MOVIES_SORT_DROPDOWN_SECTION"
    const val TOGGLE_FAVORITE_BUTTON = "TOGGLE_FAVORITE_BUTTON"

    fun moviePreview(movieTitle: String) = "MOVIE_PREVIEW_$movieTitle"

    // Detail Screen
    const val MOVIE_DETAIL_IMAGE = "MOVIE_DETAIL_IMAGE"
    const val SET_MOVIE_AS_FAVORITE_BUTTON = "SET_MOVIE_AS_FAVORITE_BUTTON"

}