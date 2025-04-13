package me.redth.douyomu.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flow
import me.redth.douyomu.R
import me.redth.douyomu.data.CardViewModel
import me.redth.douyomu.data.json.BrowseDecks
import me.redth.douyomu.ui.Loading
import me.redth.douyomu.ui.components.confirmDialogOpener
import me.redth.douyomu.ui.withState

@Composable
fun BrowsePage(viewModel: CardViewModel) {
    val browseDecksFlow = flow { emit(getDecks(viewModel)) }
    val browseDecksResult = browseDecksFlow.withState() ?: return Loading()
    val browseDecks = browseDecksResult.getOrNull()
    if (browseDecks == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                Text(
                    text = "Failed to load decks",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.width(300.dp)
                )
                Text(
                    text = browseDecksResult.exceptionOrNull()?.message ?: "Unknown error",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Browse Decks",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Browse and import decks from the internet.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
        browseDecks.decks.forEach { deck ->
            val openConfirmImportDialog = confirmDialogOpener(
                text = stringResource(R.string.import_deck_question, deck.name),
                description = "",
            ) {
                viewModel.importDeckJson(deck.url)
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    openConfirmImportDialog()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = deck.description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

const val DECKS_URL =
    "https://raw.githubusercontent.com/RedthMC/Douyomu/refs/heads/master/resources/decks/decks.json"

private suspend fun getDecks(viewModel: CardViewModel) =
    runCatching {
        viewModel.fetchJson<BrowseDecks>(DECKS_URL)
    }
