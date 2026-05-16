package com.sergebailes.bookbee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sergebailes.bookbee.core.BookBeeSection
import com.sergebailes.bookbee.core.BookBeeSections
import com.sergebailes.bookbee.ui.shelf.ShelfScreen
import com.sergebailes.bookbee.ui.shelf.ShelfViewModel
import com.sergebailes.bookbee.ui.theme.BookBeeTheme
import com.sergebailes.bookbee.ui.wishlist.WishlistScreen
import com.sergebailes.bookbee.ui.wishlist.WishlistViewModel
import kotlinx.coroutines.launch

@Composable
fun BookBeeApp(
    shelfViewModel: ShelfViewModel,
    wishlistViewModel: WishlistViewModel,
    modifier: Modifier = Modifier,
) {
    val sections = BookBeeSections.all
    val pagerState = rememberPagerState(pageCount = { sections.size })
    val scope = rememberCoroutineScope()
    val shelfUiState by shelfViewModel.uiState.collectAsState()
    val wishlistUiState by wishlistViewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Book Bee",
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    SectionTabRow(
                        sections = sections,
                        selectedPage = pagerState.currentPage,
                        onSectionSelected = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    modifier = Modifier.fillMaxSize(),
                )

                2 -> WishlistScreen(
                    state = wishlistUiState,
                    onAddWishlistItemClicked = wishlistViewModel::onAddWishlistItemClicked,
                    onEditWishlistItemClicked = wishlistViewModel::onEditWishlistItemClicked,
                    onDeleteWishlistItemClicked = wishlistViewModel::onDeleteWishlistItemClicked,
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
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
