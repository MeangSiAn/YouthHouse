package com.ams.myjeonse.feature.favorite.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.myjeonse.R
import com.ams.myjeonse.core.designsystem.theme.AppTheme

@Composable
fun FavoriteScreen(
    uiState: FavoriteContract.State,
    onAction: (FavoriteContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text = stringResource(R.string.favorite_placeholder))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoriteScreenPreview() {
    AppTheme {
        FavoriteScreen(
            uiState = FavoriteContract.State(),
            onAction = {},
        )
    }
}
