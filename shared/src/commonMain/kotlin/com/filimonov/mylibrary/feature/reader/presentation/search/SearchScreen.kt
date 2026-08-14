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
import androidx.compose.ui.unit.dp
import com.filimonov.mylibrary.core.ui.theme.AppDimension

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

    val tabs = listOf("Поиск", "Переход на страницу")

    Column(
        modifier = modifier.fillMaxWidth()
            .heightIn(max = 500.dp)
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
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
                            Text("Поиск по книге…")
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
                                        contentDescription = "Очистить поиск"
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
                                    text = "стр. ${result.globalPageIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )
                                Text(
                                    text = result.snippet,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            1 -> {
                var pageError by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimension.lg),
                    verticalArrangement = Arrangement.spacedBy(AppDimension.md)
                ) {
                    Text(
                        text = "Переход на страницу",
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
                            Text("Стр. 1..${totalPages ?: "?"}")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = pageError,
                        supportingText = {
                            if (pageError) {
                                Text("Введите страницу от 1 до $totalPages")
                            }
                        }
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val page = pageInput.toIntOrNull()

                            if (page != null && totalPages != null && page > 0 && page <= totalPages) {
                                onJumpToPage(page - 1)
                            } else {
                                pageError = true
                            }
                        }
                    ) {
                        Text("Перейти")
                    }
                }
            }
        }
    }
}
