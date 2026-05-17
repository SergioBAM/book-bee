package com.sergebailes.bookbee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.core.BookBeeSection
import com.sergebailes.bookbee.core.BookBeeSections
import com.sergebailes.bookbee.domain.usecase.LibrarySearchTarget
import com.sergebailes.bookbee.ui.history.HistoryScreen
import com.sergebailes.bookbee.ui.history.HistoryViewModel
import com.sergebailes.bookbee.ui.scan.ScanScreen
import com.sergebailes.bookbee.ui.scan.ScanViewModel
import com.sergebailes.bookbee.ui.search.LibrarySearchSurface
import com.sergebailes.bookbee.ui.search.LibrarySearchViewModel
import com.sergebailes.bookbee.ui.shelf.ShelfScreen
import com.sergebailes.bookbee.ui.shelf.ShelfViewModel
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import com.sergebailes.bookbee.ui.wishlist.WishlistScreen
import com.sergebailes.bookbee.ui.wishlist.WishlistViewModel
import kotlinx.coroutines.launch

@Composable
fun BookBeeApp(
    shelfViewModel: ShelfViewModel,
    scanViewModel: ScanViewModel,
    wishlistViewModel: WishlistViewModel,
    historyViewModel: HistoryViewModel,
    librarySearchViewModel: LibrarySearchViewModel,
    modifier: Modifier = Modifier,
) {
    val sections = BookBeeSections.all
    val pagerState = rememberPagerState(pageCount = { sections.size })
    val scope = rememberCoroutineScope()
    val shelfUiState by shelfViewModel.uiState.collectAsState()
    val scanUiState by scanViewModel.uiState.collectAsState()
    val wishlistUiState by wishlistViewModel.uiState.collectAsState()
    val historyUiState by historyViewModel.uiState.collectAsState()
    val librarySearchUiState by librarySearchViewModel.uiState.collectAsState()
    var isWishlistRowPointerActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                SectionTabRow(
                    sections = sections,
                    selectedPage = pagerState.currentPage,
                    onSectionSelected = { index ->
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LibrarySearchSurface(
                state = librarySearchUiState,
                onQueryChanged = librarySearchViewModel::onQueryChanged,
                onResultSelected = { target ->
                    scope.launch {
                        pagerState.animateScrollToPage(
                            when (target) {
                                LibrarySearchTarget.SHELF -> 0
                                LibrarySearchTarget.WISHLIST -> 2
                                LibrarySearchTarget.HISTORY -> 3
                            }
                        )
                    }
                },
            )
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isWishlistRowPointerActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> ShelfScreen(
                        state = shelfUiState,
                        onAddBookClicked = shelfViewModel::onAddBookClicked,
                        onCancelAddBook = shelfViewModel::onCancelAddBook,
                        onTitleChanged = shelfViewModel::onTitleChanged,
                        onAuthorChanged = shelfViewModel::onAuthorChanged,
                        onNotesChanged = shelfViewModel::onNotesChanged,
                        onIsbnChanged = shelfViewModel::onIsbnChanged,
                        onReadStatusChanged = shelfViewModel::onReadStatusChanged,
                        onSaveBookClicked = shelfViewModel::onSaveBookClicked,
                        onAddAnotherCopyClicked = shelfViewModel::onAddAnotherCopyClicked,
                        onUndoAddAnotherCopyClicked = shelfViewModel::onUndoAddAnotherCopyClicked,
                        onRemoveCopyClicked = shelfViewModel::onRemoveCopyClicked,
                        onConfirmArchiveClicked = shelfViewModel::onConfirmArchiveClicked,
                        onCancelArchiveClicked = shelfViewModel::onCancelArchiveClicked,
                        modifier = Modifier.fillMaxSize(),
                    )

                    1 -> ScanScreen(
                        state = scanUiState,
                        onIsbnChanged = scanViewModel::onIsbnChanged,
                        onEvaluateManualIsbnClicked = scanViewModel::onEvaluateManualIsbnClicked,
                        onCancelResultClicked = scanViewModel::onCancelResultClicked,
                        modifier = Modifier.fillMaxSize(),
                    )

                    2 -> WishlistScreen(
                        state = wishlistUiState,
                        onAddWishlistItemClicked = wishlistViewModel::onAddWishlistItemClicked,
                        onEditWishlistItemClicked = wishlistViewModel::onEditWishlistItemClicked,
                        onDeleteWishlistItemClicked = wishlistViewModel::onDeleteWishlistItemClicked,
                        onUndoWishlistRemovalClicked = wishlistViewModel::onUndoWishlistRemovalClicked,
                        onWishlistRemovalFeedbackDismissed = wishlistViewModel::onWishlistRemovalFeedbackDismissed,
                        onMoveToShelfClicked = wishlistViewModel::onMoveToShelfClicked,
                        onCancelForm = wishlistViewModel::onCancelForm,
                        onTitleChanged = wishlistViewModel::onTitleChanged,
                        onAuthorChanged = wishlistViewModel::onAuthorChanged,
                        onIsbnChanged = wishlistViewModel::onIsbnChanged,
                        onNotesChanged = wishlistViewModel::onNotesChanged,
                        onSaveWishlistItemClicked = wishlistViewModel::onSaveWishlistItemClicked,
                        onDismissOwnedOverlapConfirmation = wishlistViewModel::onDismissOwnedOverlapConfirmation,
                        onConfirmOwnedOverlapClicked = wishlistViewModel::onConfirmOwnedOverlapClicked,
                        onCancelShelfHandoff = wishlistViewModel::onCancelShelfHandoff,
                        onShelfNotesChanged = wishlistViewModel::onShelfNotesChanged,
                        onShelfReadStatusChanged = wishlistViewModel::onShelfReadStatusChanged,
                        onConfirmMoveToShelfClicked = wishlistViewModel::onConfirmMoveToShelfClicked,
                        onWishlistRowPointerActiveChanged = { isActive ->
                            isWishlistRowPointerActive = isActive
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    3 -> HistoryScreen(
                        state = historyUiState,
                        onQueryChanged = historyViewModel::onQueryChanged,
                        onEditClicked = historyViewModel::onEditClicked,
                        onEditNotesChanged = historyViewModel::onEditNotesChanged,
                        onEditReadStatusChanged = historyViewModel::onEditReadStatusChanged,
                        onCancelEditClicked = historyViewModel::onCancelEditClicked,
                        onSaveEditClicked = historyViewModel::onSaveEditClicked,
                        onRestoreClicked = historyViewModel::onRestoreClicked,
                        onHardDeleteClicked = historyViewModel::onHardDeleteClicked,
                        onCancelHardDeleteClicked = historyViewModel::onCancelHardDeleteClicked,
                        onConfirmHardDeleteClicked = historyViewModel::onConfirmHardDeleteClicked,
                        modifier = Modifier.fillMaxSize(),
                    )

                    else -> SectionPlaceholder(
                        section = sections[page],
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionPlaceholder(
    section: BookBeeSection,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = section.title.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = section.headline,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = section.supportingText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTabRow(
    sections: List<BookBeeSection>,
    selectedPage: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(
        selectedTabIndex = selectedPage,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        divider = {},
        indicator = {
            SectionTabIndicator(
                tabIndicatorScope = this,
                selectedPage = selectedPage
            )
        }
    ) {
        sections.forEachIndexed { index, section ->
            Tab(
                selected = selectedPage == index,
                onClick = { onSectionSelected(index) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedPage == index) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionTabIndicator(
    tabIndicatorScope: TabIndicatorScope,
    selectedPage: Int,
    modifier: Modifier = Modifier
) {
    val indicatorWidth = 28.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                with(tabIndicatorScope) {
                    Modifier.tabIndicatorOffset(selectedPage, false)
                }
            )
            .padding(bottom = 8.dp)
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(indicatorWidth)
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionPlaceholderPreview() {
    BookBeeTheme {
        SectionPlaceholder(section = BookBeeSections.all[1])
    }
}
