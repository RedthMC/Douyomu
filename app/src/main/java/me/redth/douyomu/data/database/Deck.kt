package me.redth.douyomu.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "decks"
)
data class Deck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val activated: Boolean = true,
)
