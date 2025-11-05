package com.dthurman.moviesaver.feature_movies.domain.use_cases

data class MoviesUseCases(
    val getMovies: GetMoviesUseCase,
    val getUserMovies: GetUserMoviesUseCase,
    val getMovieById: GetMovieByIdUseCase,
    val getPopularMovies: GetPopularMoviesUseCase,
    val searchMovies: SearchMoviesUseCase,
    val addToWatchlist: AddToWatchlistUseCase,
    val updateWatchlistStatus: UpdateWatchlistStatusUseCase,
    val markMovieAsSeen: MarkMovieAsSeenUseCase,
    val removeFromSeen: RemoveFromSeenUseCase,
    val rateMovie: RateMovieUseCase,
    val toggleFavorite: ToggleFavoriteUseCase,
    val updateFavoriteStatus: UpdateFavoriteStatusUseCase
)
