package me.redth.douyomu.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.redth.douyomu.R
import me.redth.douyomu.data.CardViewModel
import me.redth.douyomu.data.database.Card
import me.redth.douyomu.data.database.Deck
import me.redth.douyomu.ui.Loading
import me.redth.douyomu.ui.components.SlideDownPanel
import me.redth.douyomu.ui.unfocuser
import me.redth.douyomu.ui.withState


@Composable
fun SearchPage(viewModel: CardViewModel, onDismiss: () -> Unit) {
    var keyword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .unfocuser()
    ) {
        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .focusRequester(focusRequester),
            value = keyword,
            onValueChange = { keyword = it },
            singleLine = true,
            placeholder = { Text(stringResource(android.R.string.search_go)) },
            leadingIcon = {
                IconButton(
                    onClick = onDismiss,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(android.R.string.cancel),
                    )
                }
            }
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        if (keyword.isBlank()) return

        val results =
            viewModel.searchForCards(keyword.trim()).withState() ?: return@Column Loading()

        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            items(items = results, key = { it.id }) {
                WordCard(it, viewModel)
            }
        }
    }
}


@Composable
fun WordListPage(viewModel: CardViewModel) {
    val decks = viewModel.decks.withState() ?: return Loading()
    if (decks.isEmpty()) {
        viewModel.addDeck(stringResource(R.string.name_default))
        return Loading()
    }

    val lazyListState = rememberLazyListState()
    var currentDeck by remember { mutableStateOf(decks.first()) }
    var lastDeckSize by remember { mutableIntStateOf(decks.size) }

    LaunchedEffect(decks.size) {
        if (lastDeckSize < decks.size) { // added
            currentDeck = decks.last()
        } else if (lastDeckSize > decks.size) { // deleted
            currentDeck = decks.first()
        }
        lastDeckSize = decks.size
    }

    var openSearchBar by remember { mutableStateOf(false) }
//    if (openSearchBar) {
//        SearchPage(
//            viewModel = viewModel,
//            onDismiss = { openSearchBar = false }
//        )
//        return
//    }

    val expandDeckListState = remember { mutableStateOf(false) }
    var expandDeckList by expandDeckListState
    val expandDeckListAnimation by animateFloatAsState(if (expandDeckList) 1f else 0f)

    val openDialog = createNewCardDialogOpener(currentDeck, viewModel)

    val contentResolver = LocalContext.current.contentResolver
    val importer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.let {
                viewModel.importJson(it)
            } ?: run {
                viewModel.toast("bro input stream null")
            }
        } else {
            viewModel.toast("bro uri null")
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { expandDeckList = !expandDeckList },
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.decks),
                        modifier = Modifier.rotate(-90f + expandDeckListAnimation * 90f)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    DeckName(currentDeck, viewModel)
                }

                IconButton(
                    onClick = { openSearchBar = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(android.R.string.search_go),
                    )
                }
                DeckMoreDropdown(currentDeck, viewModel)
            }

            HorizontalDivider()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RectangleShape)
            ) {
                CardList(viewModel, currentDeck, lazyListState)


                // decks panel
                SlideDownPanel(expandDeckListState) {
                    decks.forEach { deck ->
                        RectButton(Icons.Default.Menu, deck.name) {
                            currentDeck = deck
                        }
                    }

                    val unnamed = stringResource(R.string.unnamed)
                    RectButton(Icons.Default.Add, stringResource(R.string.new_deck)) {
                        viewModel.addDeck(name = unnamed)
                    }

                    RectButton(Icons.Default.MailOutline, stringResource(R.string.import_deck)) {
                        importer.launch(arrayOf("application/json"))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = openDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((-16).dp, (-16).dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
        }

        AnimatedVisibility(
            visible = openSearchBar,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
        ) {
            SearchPage(
                viewModel = viewModel,
                onDismiss = { openSearchBar = false }
            )
        }
    }


}

@Composable
private fun CardList(
    viewModel: CardViewModel,
    currentDeck: Deck,
    lazyListState: LazyListState
) {
    val currentCards = viewModel.getCardsForDeck(currentDeck).withState() ?: return Loading()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        items(items = currentCards, key = { it.id }) {
            WordCard(it, viewModel)
        }
    }
}

@Composable
fun DeckName(currentDeck: Deck, viewModel: CardViewModel) {
    var deckName by remember { mutableStateOf(currentDeck.name) }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(currentDeck) {
        deckName = currentDeck.name
    }

    if (!hasFocus && deckName.isBlank()) {
        Text(
            text = "Unnamed",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            ),
        )
    }

    BasicTextField(
        value = deckName,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                hasFocus = it.hasFocus
            },
        onValueChange = {
            deckName = it
            viewModel.rename(currentDeck, deckName)
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = LocalContentColor.current,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

@Composable
fun DeckMoreDropdown(currentDeck: Deck, viewModel: CardViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val contentResolver = LocalContext.current.contentResolver
    val launcher = rememberLauncherForActivityResult(
        contract = CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.let {
                viewModel.exportToJson(it, currentDeck)
            }
        }
    }

    val openConfirmDeleteDialog = confirmDialogOpener(stringResource(R.string.delete_deck_question, currentDeck.name)) {
        viewModel.delete(currentDeck)
    }

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.export)) },
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                onClick = {
                    expanded = false
                    launcher.launch("deck.json")
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    expanded = false
                    openConfirmDeleteDialog()
                }
            )
        }
    }
}

@Composable
fun WordCard(card: Card, viewModel: CardViewModel) {
    val openDialog = dialogOpener(card, viewModel)

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            openDialog()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            key(card.furigana) {
                var scale by remember { mutableFloatStateOf(1f) }
                Text(
                    text = card.furigana,
                    fontWeight = FontWeight.Normal,
                    fontSize = TextUnit(32F, TextUnitType.Sp) * scale,
                    softWrap = false,
                    onTextLayout = { textLayoutResult ->
                        val actualWidth = textLayoutResult.multiParagraph.width
                        val originalWidth = actualWidth / scale
                        val maxWidth = textLayoutResult.size.width

                        if (originalWidth > maxWidth) {
                            scale = maxWidth / originalWidth
                        }
                    }
                )
            }
            key(card.word) {
                var scale by remember { mutableFloatStateOf(1f) }
                Text(
                    text = card.word,
                    modifier = Modifier,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(64F, TextUnitType.Sp) * scale,
                    softWrap = false,
                    onTextLayout = { textLayoutResult ->
                        val actualWidth = textLayoutResult.multiParagraph.width
                        val originalWidth = actualWidth / scale
                        val maxWidth = textLayoutResult.size.width

                        if (originalWidth > maxWidth) {
                            scale = maxWidth / originalWidth
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomDialog(
    title: String,
    initialWord: String = "",
    initialFurigana: String = "",
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var word by remember { mutableStateOf(initialWord) }
    var furigana by remember { mutableStateOf(initialFurigana) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title)
                onDelete?.let {
                    IconButton(
                        onClick = onDelete,
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text(stringResource(R.string.word)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = word.isBlank(),
                    textStyle = MaterialTheme.typography.titleLarge,
                    singleLine = true,
                )
                TextField(
                    value = furigana,
                    onValueChange = { furigana = it },
                    label = { Text(stringResource(R.string.furigana)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = furigana.isBlank(),
                    textStyle = MaterialTheme.typography.titleLarge,
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = word.isNotBlank() && furigana.isNotBlank(),
                onClick = {
                    onConfirm(word.trim(), furigana.trim())
                },
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
fun dialogOpener(card: Card, viewModel: CardViewModel): () -> Unit {
    var openDialog by remember { mutableStateOf(false) }
    if (openDialog) {
        CustomDialog(
            title = stringResource(R.string.edit),
            initialWord = card.word,
            initialFurigana = card.furigana,
            onConfirm = { word, furigana ->
                viewModel.edit(card, word, furigana)
                openDialog = false
            },
            onDelete = {
                viewModel.delete(card)
                openDialog = false
            },
            onDismiss = { openDialog = false },
        )
    }
    return { openDialog = true }
}


@Composable
fun ConfirmDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {  Text(stringResource(R.string.cannot_be_undone)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
fun confirmDialogOpener(text: String, action: () -> Unit): () -> Unit {
    var openDialog by remember { mutableStateOf(false) }
    if (openDialog) {
        ConfirmDialog(
            title = text,
            onConfirm = {
                action()
                openDialog = false
            },
            onDismiss = { openDialog = false },
        )
    }
    return { openDialog = true }
}


@Composable
fun createNewCardDialogOpener(deck: Deck, viewModel: CardViewModel): () -> Unit {
    var openDialog by remember { mutableStateOf(false) }
    if (openDialog) {
        CustomDialog(
            title = stringResource(R.string.add),
            onConfirm = { word, furigana ->
                viewModel.add(deck, word, furigana)
                openDialog = false
            },
            onDismiss = { openDialog = false },
        )
    }
    return { openDialog = true }
}
