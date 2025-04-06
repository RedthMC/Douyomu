package me.redth.douyomu.data

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.redth.douyomu.data.database.AppDatabase
import me.redth.douyomu.data.database.Card
import me.redth.douyomu.data.database.Deck
import me.redth.douyomu.data.database.ExportedCard
import me.redth.douyomu.data.database.ExportedDeck
import java.io.InputStream
import java.io.OutputStream

class CardViewModel(private val application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val cardDao = db.cardDao()
    val cards = cardDao.getAllCards()
    val decks = cardDao.getAllDecks()

    fun addDeck(name: String) = viewModelScope.launch {
        cardDao.insert(Deck(name = name))
    }

    fun getCardsForDeck(deck: Deck) = cardDao.getCardsForDeck(deck.id)

    fun getCardsFromActivatedDecks() = cardDao.getCardsFromActivatedDecks()

    fun searchForCards(keyword: String) = cardDao.searchForCards(keyword)

    fun deckCount() = cardDao.deckCount()
    fun activatedDeckCount() = cardDao.activatedDeckCount()

    fun add(deck: Deck, word: String, furigana: String) = viewModelScope.launch {
        cardDao.insert(
            Card(
                deckId = deck.id,
                word = word,
                furigana = furigana,
            )
        )
    }

    fun edit(card: Card, word: String, furigana: String) = viewModelScope.launch {
        cardDao.update(card.copy(word = word, furigana = furigana))
    }

    fun rename(deck: Deck, name: String) = viewModelScope.launch {
        cardDao.update(deck.copy(name = name))
    }

    fun setActivated(deck: Deck, activated: Boolean) = viewModelScope.launch {
        cardDao.update(deck.copy(activated = activated))
    }

    fun delete(card: Card) = viewModelScope.launch {
        cardDao.delete(card)
    }

    fun delete(deck: Deck) = viewModelScope.launch {
        cardDao.delete(deck)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun exportToJson(outputStream: OutputStream, deck: Deck) {
        CoroutineScope(Dispatchers.IO).launch {
            val cards = cardDao.getCardsForDeckSuspend(deck.id)
            val exportedObject = ExportedDeck(
                name = deck.name,
                cards = cards.map {
                    ExportedCard(
                        word = it.word,
                        furigana = it.furigana,
                    )
                }
            )

            outputStream.use {
                Json.encodeToStream(exportedObject, it)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    application,
                    "Deck exported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun toast(message: String) {
        Toast.makeText(
            application,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun importJson(inputStream: InputStream) {
        Toast.makeText(
            application,
            "Deck importing...",
            Toast.LENGTH_LONG
        ).show()

        CoroutineScope(Dispatchers.IO).launch {
            val importedDeck = inputStream.use {
                Json.decodeFromStream<ExportedDeck>(it)
            }

            println(importedDeck)

            val deckId = cardDao.insert(Deck(name = importedDeck.name)).toInt()
            importedDeck.cards.forEach {
                cardDao.insert(
                    Card(
                        deckId = deckId,
                        word = it.word,
                        furigana = it.furigana,
                    )
                )
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    application,
                    "Deck imported",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
