package com.dthurman.moviesaver.feature_movies.domain.use_cases

data class MoviesUseCases(
    val getUserMovies: GetUserMoviesUseCase,
    val getMovieById: GetMovieByIdUseCase,
    val getPopularMovies: GetPopularMoviesUseCase,
    val searchMovies: SearchMoviesUseCase,
    val updateWatchlistStatus: UpdateWatchlistStatusUseCase,
    val updateSeenStatus: UpdateSeenStatusUseCase,
    val updateRating: UpdateRatingUseCase,
    val updateFavoriteStatus: UpdateFavoriteStatusUseCase
)
