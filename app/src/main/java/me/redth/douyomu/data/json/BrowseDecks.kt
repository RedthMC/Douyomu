package me.redth.douyomu.data.json

import kotlinx.serialization.Serializable

@Serializable
data class BrowseDecks(
    val decks: List<BrowseDeck>
)

@Serializable
data class BrowseDeck(
    val url: String,
    val name: String,
    val description: String,
)
