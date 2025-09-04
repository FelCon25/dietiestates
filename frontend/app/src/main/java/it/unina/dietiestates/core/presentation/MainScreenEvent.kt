package it.unina.dietiestates.core.presentation


sealed class MainScreenEvent{
    data object OnLogout: MainScreenEvent()
}