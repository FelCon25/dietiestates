package it.unina.dietiestates.features.property.presentation.bookmarks

sealed class BookmarksScreenEvent {

    data object OnRefresh: BookmarksScreenEvent()

    data class OnError(val message: String): BookmarksScreenEvent()
}