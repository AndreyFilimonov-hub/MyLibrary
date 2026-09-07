package com.filimonov.mylibrary.feature.reader.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import mylibrary.feature.reader.generated.resources.Res
import mylibrary.feature.reader.generated.resources.clear_search
import mylibrary.feature.reader.generated.resources.go
import mylibrary.feature.reader.generated.resources.go_to_page
import mylibrary.feature.reader.generated.resources.go_to_page_tab
import mylibrary.feature.reader.generated.resources.nothing_found
import mylibrary.feature.reader.generated.resources.page_number
import mylibrary.feature.reader.generated.resources.page_placeholder
import mylibrary.feature.reader.generated.resources.page_range_error
import mylibrary.feature.reader.generated.resources.search_book
import mylibrary.feature.reader.generated.resources.search_tab
import mylibrary.feature.reader.generated.resources.search_hint
import mylibrary.feature.reader.generated.resources.search_empty_hint
import mylibrary.feature.reader.generated.resources.search_results_count
import mylibrary.feature.reader.generated.resources.close_search
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    query: String,
    results: List<SearchResult>,
    isSearching: Boolean,
    totalPages: Int?,
    onQueryChange: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    onJumpToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var pageInput by remember { mutableStateOf("") }

    var pageError by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val jumpToPage = {
        val page = pageInput.toIntOrNull()
        if (page != null && totalPages != null && page in 1..totalPages) {
            keyboard?.hide()
            onJumpToPage(page - 1)
        } else {
            pageError = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.search_book),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.close_search)
                )
            }
        }
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(stringResource(Res.string.search_tab))
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(stringResource(Res.string.go_to_page_tab))
                }
            )
        }

        when (selectedTab) {
            0 -> SearchTab(
                modifier = Modifier.weight(1f, fill = false),
                keyboard = keyboard,
                query = query,
                isSearching = isSearching,
                results = results,
                onResultClick = onResultClick,
                onQueryChange = onQueryChange
            )

            1 -> GoToPageTab(
                modifier = Modifier.weight(1f, fill = false),
                pageInput = pageInput,
                pageError = pageError,
                totalPages = totalPages,
                onValueChange = {
                    pageInput = it.filter(Char::isDigit)
                    pageError = false
                },
                jumpToPage = jumpToPage
            )
        }
    }
}

@Composable
private fun SearchTab(
    modifier: Modifier = Modifier,
    keyboard: SoftwareKeyboardController?,
    query: String,
    isSearching: Boolean,
    results: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    onQueryChange: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    stringResource(Res.string.search_book)
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            shape = RoundedCornerShape(18.dp),
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppDimension.xl)
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                Res.string.clear_search
                            )
                        )
                    }
                }
            }
        )

        if (results.isNotEmpty()) {
            Text(
                stringResource(Res.string.search_results_count, results.size),
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onResultClick(result)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(AppDimension.md)) {
                        Text(
                            text = stringResource(
                                Res.string.page_number,
                                result.globalPageIndex + 1
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(AppDimension.xs))
                        Text(
                            text = result.snippet,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            if (query.isBlank() || (results.isEmpty() && !isSearching)) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        if (query.isNotBlank()) {
                            Text(
                                text = stringResource(Res.string.nothing_found),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = stringResource(if (query.isBlank()) Res.string.search_hint else Res.string.search_empty_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoToPageTab(
    modifier: Modifier = Modifier,
    pageInput: String,
    pageError: Boolean,
    totalPages: Int?,
    onValueChange: (String) -> Unit,
    jumpToPage: () -> Unit,

) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(AppDimension.lg),
        verticalArrangement = Arrangement.spacedBy(AppDimension.md)
    ) {
        Text(
            text = stringResource(Res.string.go_to_page),
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pageInput,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    stringResource(
                        Res.string.page_placeholder,
                        totalPages?.toString() ?: "?"
                    )
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = { jumpToPage() }),
            isError = pageError,
            supportingText = {
                if (pageError) {
                    Text(
                        stringResource(
                            Res.string.page_range_error,
                            totalPages ?: "?"
                        )
                    )
                }
            }
        )
        Button(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            onClick = jumpToPage
        ) {
            Text(stringResource(Res.string.go))
        }
    }
}
