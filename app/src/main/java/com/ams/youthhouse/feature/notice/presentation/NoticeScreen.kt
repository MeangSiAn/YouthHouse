package com.ams.youthhouse.feature.notice.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ams.youthhouse.R
import com.ams.youthhouse.core.designsystem.theme.AppTheme
import com.ams.youthhouse.core.notice.domain.repository.FavoriteNoticeKey
import com.ams.youthhouse.core.notice.domain.repository.toFavoriteKey
import com.ams.youthhouse.core.ui.error.toUserMessageRes
import com.ams.youthhouse.core.notice.presentation.component.NoticeFilterChips
import com.ams.youthhouse.core.notice.presentation.component.NoticeItemCard
import com.ams.youthhouse.core.notice.presentation.model.NoticeUiModel
import com.ams.youthhouse.core.notice.presentation.model.previewNoticeUiModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun NoticeScreen(
    uiState: NoticeContract.State,
    noticeItems: LazyPagingItems<NoticeUiModel>,
    onAction: (NoticeContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // 필터를 바꾸면 목록이 통째로 갈리므로 맨 위로 되돌린다.
    //
    // 다만 "컴포지션에 다시 들어왔다"와 "필터가 바뀌었다"를 구분해야 한다.
    // 상세를 보고 뒤로 오면 이 화면이 컴포지션에 재진입하는데, 그때도 초기화하면
    // rememberLazyListState가 복원해 둔 위치를 덮어써서 목록이 맨 위로 튄다.
    // 그래서 마지막으로 적용한 필터를 따로 기억해 두고 실제로 달라졌을 때만 스크롤한다.
    val filterKey =
        "${uiState.selectedCategory}:${uiState.selectedRegion?.code.orEmpty()}:${uiState.selectedStatus}"
    var appliedFilterKey by rememberSaveable { mutableStateOf(filterKey) }

    LaunchedEffect(filterKey) {
        if (appliedFilterKey != filterKey) {
            listState.scrollToItem(0)
            appliedFilterKey = filterKey
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 기획서 SCREEN 04: 정렬이 고정(마감 임박순이 아니라 서버 순서)이라
        // 지금 무엇을 보고 있는지 헤더로 밝힌다.
        Text(
            text = stringResource(R.string.notice_list_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.semanticColors.ink,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        )

        NoticeFilterChips(
            selectedRegion = uiState.selectedRegion,
            selectedCategory = uiState.selectedCategory,
            selectedStatus = uiState.selectedStatus,
            onRegionSelected = { region ->
                onAction(NoticeContract.Action.RegionSelected(region))
            },
            onCategorySelected = { category ->
                onAction(NoticeContract.Action.CategorySelected(category))
            },
            onStatusSelected = { status ->
                onAction(NoticeContract.Action.StatusSelected(status))
            },
            modifier = Modifier.fillMaxWidth(),
        )

        NoticeList(
            noticeItems = noticeItems,
            favoriteKeys = uiState.favoriteKeys,
            onAction = onAction,
            listState = listState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NoticeList(
    noticeItems: LazyPagingItems<NoticeUiModel>,
    favoriteKeys: Set<FavoriteNoticeKey>,
    onAction: (NoticeContract.Action) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val refreshState = noticeItems.loadState.refresh
    val appendState = noticeItems.loadState.append

    // refresh 도중 잠깐 빈 화면이 깜빡이지 않도록 endOfPaginationReached까지 함께 본다.
    val isEmpty = refreshState is LoadState.NotLoading &&
        appendState.endOfPaginationReached &&
        noticeItems.itemCount == 0

    Box(modifier = modifier.fillMaxSize()) {
        when {
            refreshState is LoadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            refreshState is LoadState.Error -> {
                NoticeErrorContent(
                    throwable = refreshState.error,
                    onRetryClick = noticeItems::retry,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            isEmpty -> {
                Text(
                    text = stringResource(R.string.notice_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // key를 지정하지 않는다 — 이 API는 같은 공고를 시군구별로 쪼개 보내고
                    // 32개 필드가 완전히 동일한 행도 있어 고유 키를 만들 수 없다.
                    // 중복 키를 주면 Compose가 "Key was already used"로 크래시한다.
                    items(count = noticeItems.itemCount) { index ->
                        noticeItems[index]?.let { notice ->
                            NoticeItemCard(
                                notice = notice,
                                onClick = {
                                    onAction(NoticeContract.Action.NoticeClicked(notice))
                                },
                                isFavorite = notice.source.toFavoriteKey() in favoriteKeys,
                                onFavoriteClick = {
                                    onAction(NoticeContract.Action.FavoriteClicked(notice))
                                },
                            )
                        }
                    }

                    if (appendState is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (appendState is LoadState.Error) {
                        item {
                            NoticeErrorContent(
                                throwable = appendState.error,
                                onRetryClick = noticeItems::retry,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeErrorContent(
    throwable: Throwable,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(throwable.toUserMessageRes()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetryClick) {
            Text(text = stringResource(R.string.retry))
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun NoticeScreenPreview() {
    val previewNotice = previewNoticeUiModel()

    AppTheme {
        NoticeScreen(
            uiState = NoticeContract.State(),
            noticeItems = flowOf(PagingData.from(listOf(previewNotice)))
                .collectAsLazyPagingItems(),
            onAction = {},
        )
    }
}
