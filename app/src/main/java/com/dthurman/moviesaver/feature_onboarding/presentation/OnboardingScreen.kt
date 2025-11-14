package com.dthurman.moviesaver.feature_onboarding.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dthurman.moviesaver.feature_onboarding.presentation.components.OnboardingNavigationButtons
import com.dthurman.moviesaver.feature_onboarding.presentation.components.OnboardingPage

@Composable
fun OnboardingDialog(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        OnboardingContent(
            currentPage = uiState.currentPage,
            onNextPage = { viewModel.onEvent(OnboardingEvent.NextPage) },
            onPreviousPage = { viewModel.onEvent(OnboardingEvent.PreviousPage) },
            onComplete = { viewModel.onEvent(OnboardingEvent.CompleteOnboarding) },
            onPageChange = { page -> viewModel.onEvent(OnboardingEvent.GoToPage(page)) },
            modifier = modifier
        )
    }
}

@Composable
private fun OnboardingContent(
    currentPage: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onComplete: () -> Unit,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPages = 4
    val pagerState = rememberPagerState(pageCount = { totalPages })

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentPage) {
            onPageChange(pagerState.currentPage)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    OnboardingPage(page = page)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OnboardingNavigationButtons(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPreviousClick = {
                        if (currentPage > 0) {
                            onPreviousPage()
                        }
                    },
                    onNextClick = onNextPage,
                    onCompleteClick = onComplete
                )
            }
        }
    }
}

