package me.redth.douyomu.ui.screens

import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.redth.douyomu.R
import me.redth.douyomu.data.CardViewModel
import me.redth.douyomu.ui.Loading
import me.redth.douyomu.ui.components.SlideDownPanel
import me.redth.douyomu.ui.withState
import kotlin.math.roundToInt


@Composable
fun Home(viewModel: CardViewModel) {
    val decks = viewModel.decks.withState() ?: return Loading()
    val deckCount = viewModel.deckCount().withState() ?: return Loading()
    val activatedDeckCount = viewModel.activatedDeckCount().withState() ?: return Loading()

    val expandActivatedDecksState = remember { mutableStateOf(false) }
    var expandActivatedDecks by expandActivatedDecksState
    val expandActivatedDecksAnimation by animateFloatAsState(if (expandActivatedDecks) 1f else 0f)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { expandActivatedDecks = !expandActivatedDecks },
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.decks),
                    modifier = Modifier.rotate(-90f + expandActivatedDecksAnimation * 90f)
                )
            }
            Text(stringResource(R.string.question_bank, activatedDeckCount, deckCount))
        }
        HorizontalDivider()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
        ) {
            var updater by remember { mutableIntStateOf(0) }
            LaunchedEffect(expandActivatedDecks) {
                if (!expandActivatedDecks) updater++
            }
            key(updater) { // update when closing the panel
                QASection(viewModel)
            }

            SlideDownPanel(expandActivatedDecksState) {
                decks.forEach { deck ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusable()
                            .clickable {
                                viewModel.setActivated(deck, !deck.activated)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = deck.activated,
                                onCheckedChange = null,
                            )
                            Text(
                                text = deck.name,
                                fontSize = 16.sp,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun QASection(viewModel: CardViewModel) {
    val cards = viewModel.getCardsFromActivatedDecks().withState() ?: return Loading()
    var word by remember { mutableStateOf(cards.randomOrNull()) }

    var textFieldInput by remember { mutableStateOf("") }

    val shakeX = remember { Animatable(0f) }
    val shakeY = remember { Animatable(0f) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val errorColor = MaterialTheme.colorScheme.error
    val correctColor = Color(0xFF77FF77)
    val bgColor = remember { Animatable(backgroundColor) }

    val scope = rememberCoroutineScope()

    val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrationEnabled = LocalContext.current.settings.data.withState()?.shouldVibrate ?: true

    fun shaking() {
        if (vibrationEnabled) vibrator.vibrate(100)

        scope.launch {
            shakeX.animateTo(-10f, animationSpec = tween(100))
            shakeX.animateTo(10f, animationSpec = tween(100))
            shakeX.animateTo(0f, animationSpec = tween(100))
        }
        scope.launch {
            bgColor.animateTo(errorColor, animationSpec = tween(100))
            bgColor.animateTo(backgroundColor, animationSpec = tween(200))
        }
    }

    fun nodding() {
        if (vibrationEnabled) vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)

        scope.launch {
            shakeY.animateTo(20f, animationSpec = tween(150))
            shakeY.animateTo(0f, animationSpec = tween(150))
        }
        scope.launch {
            bgColor.animateTo(correctColor, animationSpec = tween(100))
            bgColor.animateTo(backgroundColor, animationSpec = tween(200))
        }
    }

    fun onSubmit() {
        if (textFieldInput.trim() == word?.pronunciation) {
            word = cards.randomOrNull()
            textFieldInput = ""
            nodding()
        } else {
            shaking()
            textFieldInput = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor.value)
            .offset {
                IntOffset(
                    x = shakeX.value.dp.toPx().roundToInt(),
                    y = shakeY.value.dp.toPx().roundToInt(),
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val capturedWord = word
        if (capturedWord == null) {
            Text(
                text = stringResource(R.string.no_cards),
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(96F, TextUnitType.Sp),
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        var showHint by remember { mutableStateOf(false) }

        LaunchedEffect(capturedWord) {
            if (showHint) {
                showHint = false
            }
        }

        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            key(capturedWord.pronunciation) {
                var showHintActually by remember { mutableStateOf(false) } // prevent delayed hide hint

                LaunchedEffect(showHint) {
                    if (showHint) showHintActually = true
                }

                if (showHintActually) {
                    var scale by remember { mutableFloatStateOf(1f) }
                    Text(
                        modifier = Modifier.offset(y = (-12).dp),
                        text = capturedWord.pronunciation,
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
            }


            key(capturedWord.word) {
                var scale by remember { mutableFloatStateOf(1f) }

                Text(
                    text = capturedWord.word,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(96F, TextUnitType.Sp) * scale,
                    softWrap = false,
                    onTextLayout = { textLayoutResult ->
                        val actualWidth = textLayoutResult.multiParagraph.width
                        val originalWidth = actualWidth / scale
                        val maxWidth = textLayoutResult.size.width

                        if (originalWidth > maxWidth) {
                            scale = maxWidth / originalWidth
                        }
                    },
                )
            }
        }

        TextField(
            label = { Text(text = stringResource(R.string.pronunciation)) },
            value = textFieldInput,
            onValueChange = { textFieldInput = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { showHint = true },
                enabled = !showHint,
            ) {
                Text(text = stringResource(R.string.hint))
            }
            Button(onClick = { onSubmit() }) {
                Text(text = stringResource(R.string.confirm))
            }
        }
    }
}
