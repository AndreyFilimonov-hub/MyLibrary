package com.filimonov.mylibrary.feature.reader.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.filimonov.mylibrary.core.ui.theme.AppDimension
import mylibrary.shared.generated.resources.Res
import mylibrary.shared.generated.resources.*
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
    onJumpToPage: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var pageInput by remember { mutableStateOf("") }

    var pageError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab
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
            0 -> {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimension.sm),
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = {
                            Text(
                                stringResource(Res.string.search_book)
                            )
                        },
                        singleLine = true,
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

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = AppDimension.sm)
                    ) {
                        items(results) { result ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onResultClick(result)
                                    }
                                    .padding(AppDimension.md)
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.page_number,
                                        result.globalPageIndex + 1
                                    ),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(
                                    modifier = Modifier.height(AppDimension.xs)
                                )
                                Text(
                                    text = result.snippet,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            HorizontalDivider()
                        }
                        if (query.isNotBlank() && results.isEmpty() && !isSearching) {
                            item {
                                Spacer(Modifier.height(AppDimension.lg))
                            }
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(Res.string.nothing_found),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimension.lg),
                    verticalArrangement = Arrangement.spacedBy(AppDimension.md)
                ) {
                    Text(
                        text = stringResource(Res.string.go_to_page),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = pageInput,
                        onValueChange = {
                            pageInput = it.filter(Char::isDigit)
                            pageError = false
                        },
                        placeholder = {
                            Text(
                                stringResource(
                                    Res.string.page_placeholder,
                                    totalPages?.toString() ?: "?"
                                )
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
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
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val page = pageInput.toIntOrNull()

                            if (
                                page != null &&
                                totalPages != null &&
                                page in 1..totalPages
                            ) {
                                onJumpToPage(page - 1)
                            } else {
                                pageError = true
                            }
                        }
                    ) {
                        Text(
                            stringResource(Res.string.go)
                        )
                    }
                }
            }
        }
    }
}
