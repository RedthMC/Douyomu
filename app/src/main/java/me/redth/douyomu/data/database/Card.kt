package me.redth.douyomu.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deckId: Int,
    val word: String,
    val furigana: String
)
