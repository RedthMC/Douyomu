package me.redth.douyomu.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Card::class, Deck::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: Create the new decks table
        db.execSQL(
            """
            CREATE TABLE decks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Insert the default deck
        db.execSQL(
            "INSERT INTO decks (name) VALUES ('Default')"
        )

        // Step 3: Add the new cards table with deckId as a foreign key
        db.execSQL(
            """
            CREATE TABLE cards_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                deckId INTEGER NOT NULL,
                question TEXT NOT NULL,
                answer TEXT NOT NULL,
                FOREIGN KEY(deckId) REFERENCES decks(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // Step 4: Get the default deck ID (should be 1 since it's auto-increment)
        val cursor = db.query("SELECT id FROM decks WHERE name = 'Default'")
        var defaultDeckId = 1
        if (cursor.moveToFirst()) {
            defaultDeckId = cursor.getInt(0)
        }
        cursor.close()

        // Step 5: Copy old card data and assign them to the default deck
        db.execSQL(
            """
            INSERT INTO cards_new (id, deckId, question, answer)
            SELECT id, $defaultDeckId, question, answer FROM cards
            """.trimIndent()
        )

        // Step 6: Remove old cards table
        db.execSQL("DROP TABLE cards")

        // Step 7: Rename new cards table to original name
        db.execSQL("ALTER TABLE cards_new RENAME TO cards")
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE decks ADD COLUMN activated INTEGER NOT NULL DEFAULT 1")
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new table with updated column names
        db.execSQL("""
            CREATE TABLE cards_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                deckId INTEGER NOT NULL,
                word TEXT NOT NULL,
                furigana TEXT NOT NULL,
                FOREIGN KEY(deckId) REFERENCES decks(id) ON DELETE CASCADE
            )
        """)

        // 2. Copy data from old table
        db.execSQL("""
            INSERT INTO cards_new (id, deckId, word, furigana)
            SELECT id, deckId, question, answer FROM cards
        """)

        // 3. Drop old table
        db.execSQL("DROP TABLE cards")

        // 4. Rename new table
        db.execSQL("ALTER TABLE cards_new RENAME TO cards")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new table with updated column names
        db.execSQL("""
            CREATE TABLE cards_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                deckId INTEGER NOT NULL,
                word TEXT NOT NULL,
                pronunciation TEXT NOT NULL,
                FOREIGN KEY(deckId) REFERENCES decks(id) ON DELETE CASCADE
            )
        """)

        // 2. Copy data from old table
        db.execSQL("""
            INSERT INTO cards_new (id, deckId, word, pronunciation)
            SELECT id, deckId, word, furigana FROM cards
        """)

        // 3. Drop old table
        db.execSQL("DROP TABLE cards")

        // 4. Rename new table
        db.execSQL("ALTER TABLE cards_new RENAME TO cards")
    }
}



