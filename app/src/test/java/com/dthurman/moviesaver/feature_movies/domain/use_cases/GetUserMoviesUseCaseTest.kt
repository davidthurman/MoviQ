package com.dthurman.moviesaver.feature_movies.domain.use_cases

import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.dthurman.moviesaver.feature_movies.domain.util.MovieFilter
import com.dthurman.moviesaver.feature_movies.domain.util.MovieOrder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetUserMoviesUseCaseTest {

    private lateinit var getUserMoviesUseCase: GetUserMoviesUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        getUserMoviesUseCase = GetUserMoviesUseCase(fakeMovieRepository)
    }

    // Seen Movie Tests
    @Test
    fun `Get Seen Movies by title, ascending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.TITLE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtMost(movies[i + 1].title)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by title, descending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.TITLE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtLeast(movies[i + 1].title)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by release date, ascending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.RELEASE_DATE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtMost(movies[i + 1].releaseDate)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by release date, descending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.RELEASE_DATE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtLeast(movies[i + 1].releaseDate)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by added date, ascending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.DATE_ADDED_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtMost(movies[i + 1].addedAt)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by added date, descending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.DATE_ADDED_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtLeast(movies[i + 1].addedAt)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by rating, ascending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.RATING_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtMost(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    @Test
    fun `Get Seen Movies by rating, descending, correct order and seen status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.SeenMovies(MovieOrder.RATING_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtLeast(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isSeen).isTrue()
        }
    }

    // Watchlist Movies Tests
    @Test
    fun `Get Watchlist Movies by title, ascending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.TITLE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtMost(movies[i + 1].title)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by title, descending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.TITLE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtLeast(movies[i + 1].title)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by release date, ascending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.RELEASE_DATE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtMost(movies[i + 1].releaseDate)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by release date, descending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.RELEASE_DATE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtLeast(movies[i + 1].releaseDate)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by added date, ascending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.DATE_ADDED_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtMost(movies[i + 1].addedAt)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by added date, descending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.DATE_ADDED_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtLeast(movies[i + 1].addedAt)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by rating, ascending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.RATING_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtMost(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    @Test
    fun `Get Watchlist Movies by rating, descending, correct order and watchlist status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.WatchlistMovies(MovieOrder.RATING_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtLeast(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isWatchlist).isTrue()
        }
    }

    // Favorite Movies Tests
    @Test
    fun `Get Favorite Movies by title, ascending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.TITLE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtMost(movies[i + 1].title)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by title, descending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.TITLE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].title).isAtLeast(movies[i + 1].title)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by release date, ascending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.RELEASE_DATE_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtMost(movies[i + 1].releaseDate)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by release date, descending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.RELEASE_DATE_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].releaseDate).isAtLeast(movies[i + 1].releaseDate)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by added date, ascending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.DATE_ADDED_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtMost(movies[i + 1].addedAt)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by added date, descending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.DATE_ADDED_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].addedAt).isAtLeast(movies[i + 1].addedAt)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by rating, ascending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.RATING_ASC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtMost(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }

    @Test
    fun `Get Favorite Movies by rating, descending, correct order and favorite status`() = runBlocking {
        val movies = getUserMoviesUseCase.invoke(MovieFilter.FavoriteMovies(MovieOrder.RATING_DESC)).first()
        for (i in 0..movies.size - 2) {
            assertThat(movies[i].rating ?: 0f).isAtLeast(movies[i + 1].rating ?: 0f)
            assertThat(movies[i].isFavorite).isTrue()
        }
    }
}