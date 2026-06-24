package com.trobat.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trobat.R
import com.trobat.ui.theme.BackgroundPrincipal
import com.trobat.ui.theme.TrobatBackground
import com.trobat.ui.theme.TrobatPurple
import com.trobat.ui.theme.TrobatPurpleSoft
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val slides = listOf(
        OnboardingSlide(
            icon = Icons.Filled.PersonSearch,
            title = stringResource(R.string.onboarding_slide1_titulo),
            description = stringResource(R.string.onboarding_slide1_desc)
        ),
        OnboardingSlide(
            icon = Icons.Filled.CameraAlt,
            title = stringResource(R.string.onboarding_slide2_titulo),
            description = stringResource(R.string.onboarding_slide2_desc)
        ),
        OnboardingSlide(
            icon = Icons.Filled.People,
            title = stringResource(R.string.onboarding_slide3_titulo),
            description = stringResource(R.string.onboarding_slide3_desc)
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundPrincipal
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onFinish) {
                        Text(stringResource(R.string.accion_saltar), color = TrobatBackground.copy(alpha = 0.7f))
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(slides[page])
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                repeat(slides.size) { index ->
                    val dotWidth by animateDpAsState(
                        targetValue = if (pagerState.currentPage == index) 24.dp else 8.dp,
                        label = "dot_width_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) TrobatBackground
                                else TrobatBackground.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrobatPurple)
            ) {
                Text(
                    text = stringResource(if (isLastPage) R.string.accion_comenzar else R.string.accion_siguiente),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(slide: OnboardingSlide) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = slide.icon,
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            tint = TrobatPurpleSoft
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium,
            color = TrobatBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TrobatBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
