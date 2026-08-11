package com.ams.myjeonse.feature.notice.presentation

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
import com.ams.myjeonse.R
import com.ams.myjeonse.core.designsystem.theme.AppTheme
import com.ams.myjeonse.core.ui.error.toUserMessageRes
import com.ams.myjeonse.core.notice.presentation.component.NoticeItemCard
import com.ams.myjeonse.core.notice.presentation.component.NoticeRegionSpinner
import com.ams.myjeonse.core.notice.presentation.model.NoticeUiModel
import com.ams.myjeonse.core.notice.presentation.model.previewNoticeUiModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun NoticeScreen(
    uiState: NoticeContract.State,
    noticeItems: LazyPagingItems<NoticeUiModel>,
    onAction: (NoticeContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // 지역을 바꾸면 목록이 통째로 갈리므로 이전 스크롤 위치를 들고 있으면 중간에 떨어진다.
    LaunchedEffect(uiState.selectedRegion) {
        listState.scrollToItem(0)
    }

    Column(modifier = modifier.fillMaxSize()) {
        NoticeRegionSpinner(
            selectedRegion = uiState.selectedRegion,
            onRegionSelected = { region ->
                onAction(NoticeContract.Action.RegionSelected(region))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        NoticeList(
            noticeItems = noticeItems,
            onAction = onAction,
            listState = listState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NoticeList(
    noticeItems: LazyPagingItems<NoticeUiModel>,
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
