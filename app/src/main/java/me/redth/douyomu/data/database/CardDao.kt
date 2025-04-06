package me.redth.douyomu.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card)

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: Int): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deck: Deck): Long

    @Update
    suspend fun update(deck: Deck)

    @Delete
    suspend fun delete(deck: Deck)

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Int): Deck?

    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<Card>>

    @Query(
        """
        SELECT cards.* 
        FROM cards 
        JOIN decks ON cards.deckId = decks.id 
        WHERE decks.activated = 1
        """
    )
    fun getCardsFromActivatedDecks(): Flow<List<Card>>


    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: Int): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE word LIKE '%' || :keyword || '%' OR furigana LIKE '%' || :keyword || '%'")
    fun searchForCards(keyword: String): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    suspend fun getCardsForDeckSuspend(deckId: Int): List<Card>

    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<Deck>>

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun size(): Int

    @Query("SELECT COUNT(*) FROM decks")
    fun deckCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM decks WHERE activated = 1")
    fun activatedDeckCount(): Flow<Int>

}
