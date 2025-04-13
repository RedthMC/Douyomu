package me.redth.douyomu.data.json

import kotlinx.serialization.Serializable


@Serializable
data class ExportedDeck(
    val name: String,
    val cards: List<ExportedCard>,
)

@Serializable
data class ExportedCard(
    val word: String,
    val pronunciation: String,
)