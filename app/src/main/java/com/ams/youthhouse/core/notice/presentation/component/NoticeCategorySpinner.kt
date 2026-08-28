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
import com.ams.youthhouse.core.notice.domain.model.NoticeCategory
import com.ams.youthhouse.core.notice.presentation.model.labelRes

/**
 * 공고 분야 선택 스피너.
 *
 * 지역과 달리 "전체"가 없다. 임대와 분양은 서로 다른 오퍼레이션이고
 * 이 API에는 정렬 파라미터가 없어 두 목록을 합쳐도 순서에 의미가 없기 때문이다.
 * 통합해서 보는 경로는 홈이 담당한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeCategorySpinner(
    selectedCategory: NoticeCategory,
    onCategorySelected: (NoticeCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = stringResource(selectedCategory.labelRes),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(text = stringResource(R.string.notice_category_label)) },
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
            NoticeCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(category.labelRes)) },
                    onClick = {
                        expanded = false
                        onCategorySelected(category)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeCategorySpinnerPreview() {
    AppTheme {
        NoticeCategorySpinner(
            selectedCategory = NoticeCategory.SALE,
            onCategorySelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
