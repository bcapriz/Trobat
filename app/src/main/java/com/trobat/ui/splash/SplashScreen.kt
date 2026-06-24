package com.trobat.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trobat.R
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.trobat.ui.theme.BackgroundPrincipal
import com.trobat.ui.theme.TrobatBackground

@Composable
fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundPrincipal
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.trobatlogooscuro),
                    contentDescription = stringResource(R.string.splash_logo_desc),
                    modifier = Modifier.size(540.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.splash_eslogan),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrobatBackground,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.splash_creditos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrobatBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.splash_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = TrobatBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
