package com.ams.youthhouse.core.notice.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.domain.model.NoticeRegion
import com.ams.youthhouse.core.notice.domain.model.NoticeStatusFilter
import com.ams.youthhouse.core.notice.presentation.model.labelRes

/**
 * 공고 목록의 필터 칩 두 줄.
 *
 * 지역·분야가 첫 줄, 접수 상태가 둘째 줄이다. 호출부는 세로로 쌓이는 곳
 * (Column 등)에 놓아야 한다.
 *
 * dev2.0 기획은 필터를 스피너가 아니라 칩으로 둔다. 선택지가 두세 개뿐이라
 * 드롭다운을 여는 한 번의 탭이 낭비이고, 무엇보다 **지금 무엇이 걸려 있는지가
 * 목록 위에 그대로 보여야** 결과 건수를 납득할 수 있기 때문이다.
 *
 * 지역만 예외로 드롭다운을 유지한다. 17개를 칩으로 늘어놓으면 가로 스크롤이
 * 길어져 다른 필터가 화면 밖으로 밀린다.
 */
@Composable
fun NoticeFilterChips(
    selectedRegion: NoticeRegion?,
    selectedCategory: NoticeCategory,
    selectedStatus: NoticeStatusFilter,
    onRegionSelected: (NoticeRegion?) -> Unit,
    onCategorySelected: (NoticeCategory) -> Unit,
    onStatusSelected: (NoticeStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipRow(modifier = modifier) {
        NoticeRegionChip(
            selectedRegion = selectedRegion,
            onRegionSelected = onRegionSelected,
        )
        NoticeCategory.entries.forEach { category ->
            NoticeFilterChip(
                selected = category == selectedCategory,
                label = stringResource(category.labelRes),
                onClick = { onCategorySelected(category) },
            )
        }
    }

    ChipRow(modifier = modifier) {
        NoticeStatusFilter.entries.forEach { status ->
            NoticeFilterChip(
                selected = status == selectedStatus,
                label = stringResource(status.labelRes),
                onClick = { onStatusSelected(status) },
            )
        }
    }
}

/** 칩이 화면 폭을 넘어가면 줄바꿈 대신 가로로 스크롤한다. 줄 수가 늘면 목록이 그만큼 밀린다. */
@Composable
private fun ChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = { content() },
    )
}

/** 지역 선택. 칩을 앵커로 드롭다운을 연다. */
@Composable
private fun NoticeRegionChip(
    selectedRegion: NoticeRegion?,
    onRegionSelected: (NoticeRegion?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResource(R.string.notice_region_all)

    Row {
        NoticeFilterChip(
            // 지역은 "전체"도 하나의 선택이라 항상 채워 둔다. 비워 두면 필터가 꺼진 것처럼 읽힌다.
            selected = true,
            label = selectedRegion?.regionName ?: allLabel,
            onClick = { expanded = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 2.dp),
                )
            },
        )

        DropdownMenu(
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

@Composable
private fun NoticeFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.large,
        colors = noticeFilterChipColors(),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = AppTheme.semanticColors.line,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

/** 선택 시 코발트로 채우고, 아닐 때는 테두리만 남긴다(기획서 `.chip`). */
@Composable
private fun noticeFilterChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    labelColor = AppTheme.semanticColors.ink70,
    iconColor = AppTheme.semanticColors.ink45,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
)

/** 접수 상태 필터의 표시 문구. */
private val NoticeStatusFilter.labelRes: Int
    get() = when (this) {
        NoticeStatusFilter.OPEN -> R.string.notice_status_open
        NoticeStatusFilter.UPCOMING -> R.string.notice_status_upcoming
        NoticeStatusFilter.ALL -> R.string.notice_status_all
    }

@Preview(showBackground = true)
@Composable
private fun NoticeFilterChipsPreview() {
    AppTheme {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            NoticeFilterChips(
                selectedRegion = NoticeRegion.SEOUL,
                selectedCategory = NoticeCategory.RENTAL,
                selectedStatus = NoticeStatusFilter.OPEN,
                onRegionSelected = {},
                onCategorySelected = {},
                onStatusSelected = {},
            )
        }
    }
}
