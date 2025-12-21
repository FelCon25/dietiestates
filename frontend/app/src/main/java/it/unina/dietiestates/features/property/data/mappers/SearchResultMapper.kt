package it.unina.dietiestates.features.property.data.mappers

import it.unina.dietiestates.features.property.data.dto.SearchResultDto
import it.unina.dietiestates.features.property.domain.SearchResult

fun SearchResultDto.toSearchResult(): SearchResult {
    return SearchResult(
        items = items.map { it.toProperty() },
        total = total,
        page = page,
        pageSize = pageSize,
        totalPages = totalPages,
        hasMore = hasMore
    )
}

