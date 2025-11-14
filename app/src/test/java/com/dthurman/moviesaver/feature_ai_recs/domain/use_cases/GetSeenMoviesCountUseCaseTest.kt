package com.dthurman.moviesaver.feature_ai_recs.domain.use_cases

import com.dthurman.moviesaver.core.domain.model.Movie
import com.dthurman.moviesaver.feature_movies.data.repository.FakeMovieRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetSeenMoviesCountUseCaseTest {

    private lateinit var getSeenMoviesCountUseCase: GetSeenMoviesCountUseCase
    private lateinit var fakeMovieRepository: FakeMovieRepository

    private val testMovies = listOf(
        Movie(
            id = 1,
            title = "Seen Movie 1",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-01",
            overview = "Test overview 1",
            isSeen = true
        ),
        Movie(
            id = 2,
            title = "Seen Movie 2",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-02",
            overview = "Test overview 2",
            isSeen = true
        ),
        Movie(
            id = 3,
            title = "Watchlist Movie",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-03",
            overview = "Test overview 3",
            isWatchlist = true
        ),
        Movie(
            id = 4,
            title = "Seen Movie 3",
            posterUrl = "",
            backdropUrl = "",
            releaseDate = "2024-01-04",
            overview = "Test overview 4",
            isSeen = true
        )
    )

    @Before
    fun setUp() {
        fakeMovieRepository = FakeMovieRepository()
        getSeenMoviesCountUseCase = GetSeenMoviesCountUseCase(
            movieRepository = fakeMovieRepository
        )
    }

    @Test
    fun `get seen movies count returns correct count`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            fakeMovieRepository.movies.addAll(testMovies)

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(3)
        }
    }

    @Test
    fun `get seen movies count returns zero when no seen movies`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            fakeMovieRepository.movies.add(testMovies[2])

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun `get seen movies count returns zero for empty repository`() {
        runBlocking {
            fakeMovieRepository.movies.clear()

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun `get seen movies count updates when movies are added`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            val flow = getSeenMoviesCountUseCase.invoke()

            val initialCount = flow.first()
            assertThat(initialCount).isEqualTo(0)

            fakeMovieRepository.updateSeenStatus(testMovies[0], true)
            val updatedCount = flow.first()
            assertThat(updatedCount).isEqualTo(1)
        }
    }

    @Test
    fun `get seen movies count updates when movies are removed`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            fakeMovieRepository.movies.addAll(testMovies)
            val flow = getSeenMoviesCountUseCase.invoke()

            val initialCount = flow.first()
            assertThat(initialCount).isEqualTo(3)

            fakeMovieRepository.updateSeenStatus(testMovies[0], false)
            val updatedCount = flow.first()
            assertThat(updatedCount).isEqualTo(2)
        }
    }

    @Test
    fun `get seen movies count only counts seen movies not watchlist`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            fakeMovieRepository.movies.add(testMovies[2])

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun `get seen movies count with single seen movie returns one`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            fakeMovieRepository.movies.add(testMovies[0])

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(1)
        }
    }

    @Test
    fun `get seen movies count with all movies seen returns total count`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            val allSeenMovies = testMovies.map { it.copy(isSeen = true) }
            fakeMovieRepository.movies.addAll(allSeenMovies)

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(4)
        }
    }

    @Test
    fun `get seen movies count excludes movies with only favorite status`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            val favoriteMovie = Movie(
                id = 5,
                title = "Favorite Movie",
                posterUrl = "",
                backdropUrl = "",
                releaseDate = "2024-01-05",
                overview = "Test overview 5",
                isFavorite = true,
                isSeen = false
            )
            fakeMovieRepository.movies.add(favoriteMovie)

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun `get seen movies count includes movies that are both seen and favorite`() {
        runBlocking {
            fakeMovieRepository.movies.clear()
            val seenAndFavoriteMovie = Movie(
                id = 6,
                title = "Seen and Favorite Movie",
                posterUrl = "",
                backdropUrl = "",
                releaseDate = "2024-01-06",
                overview = "Test overview 6",
                isFavorite = true,
                isSeen = true
            )
            fakeMovieRepository.movies.add(seenAndFavoriteMovie)

            val count = getSeenMoviesCountUseCase.invoke().first()

            assertThat(count).isEqualTo(1)
        }
    }
}


