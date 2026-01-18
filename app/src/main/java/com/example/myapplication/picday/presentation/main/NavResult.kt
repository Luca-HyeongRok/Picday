package com.example.myapplication.picday.presentation.main

data class NavResult(
    val backStack: List<MainDestination>,
    val effects: List<MainNavEffect> = emptyList()
)
