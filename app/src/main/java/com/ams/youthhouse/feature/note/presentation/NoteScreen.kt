package com.ams.youthhouse.feature.note.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppTheme

@Composable
fun NoteScreen(
    uiState: NoteContract.State,
    onAction: (NoteContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text = stringResource(R.string.note_placeholder))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteScreenPreview() {
    AppTheme {
        NoteScreen(
            uiState = NoteContract.State(),
            onAction = {},
        )
    }
}
