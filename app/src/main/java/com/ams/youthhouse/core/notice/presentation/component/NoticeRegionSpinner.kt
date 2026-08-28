package com.ams.youthhouse.core.notice.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion

/**
 * 광역시도 선택 스피너. 선택값은 조회의 `brtcCode`가 된다.
 *
 * `null`은 "전체"를 뜻한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeRegionSpinner(
    selectedRegion: NoticeRegion?,
    onRegionSelected: (NoticeRegion?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResource(R.string.notice_region_all)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedRegion?.regionName ?: allLabel,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(text = stringResource(R.string.notice_region_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                ),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(text = allLabel) },
                onClick = {
                    expanded = false
                    onRegionSelected(null)
                },
            )

            NoticeRegion.entries.forEach { region ->
                DropdownMenuItem(
                    text = { Text(text = region.regionName) },
                    onClick = {
                        expanded = false
                        onRegionSelected(region)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeRegionSpinnerPreview() {
    AppTheme {
        NoticeRegionSpinner(
            selectedRegion = NoticeRegion.GYEONGGI,
            onRegionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
