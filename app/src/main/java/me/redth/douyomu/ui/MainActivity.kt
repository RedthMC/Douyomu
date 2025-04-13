package me.redth.douyomu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.redth.douyomu.R
import me.redth.douyomu.data.CardViewModel
import me.redth.douyomu.ui.screens.BrowsePage
import me.redth.douyomu.ui.screens.Home
import me.redth.douyomu.ui.screens.SettingsPage
import me.redth.douyomu.ui.screens.Theme
import me.redth.douyomu.ui.screens.WordListPage
import me.redth.douyomu.ui.screens.settings
import me.redth.douyomu.ui.screens.theme
import me.redth.douyomu.ui.theme.DouyomuTheme

/**
 * Apply state management to a Flow<T> and return the latest value.
 */
@Composable
fun <T> Flow<T>.withState(): T? = collectAsState(initial = null).value

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dataStore = LocalContext.current.settings
            val settings = dataStore.data.withState() ?: return@setContent Loading()
            val isDarkTheme = when (settings.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            val viewModel: CardViewModel = viewModel()

            DouyomuTheme(darkTheme = isDarkTheme) {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun Modifier.unfocuser() = LocalFocusManager.current.let { focusManager ->
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        focusManager.clearFocus()
    }
}

@Composable
fun AppNavigation(viewModel: CardViewModel) {
    val pageState = rememberPagerState { 4 }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(pageState.currentPage) {
        focusManager.clearFocus()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(pageState) },
    ) { innerPadding ->
        HorizontalPager(
            modifier = Modifier
                .padding(innerPadding)
                .unfocuser(),
            state = pageState,
        ) {
            when (it) {
                0 -> Home(viewModel)
                1 -> WordListPage(viewModel)
                2 -> BrowsePage(viewModel)
                3 -> SettingsPage()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(pagerState: PagerState) {
    val items = listOf(
        Icons.Default.Home to R.string.home,
        Icons.AutoMirrored.Filled.List to R.string.word_list,
        Icons.Default.ShoppingCart to R.string.browse,
        Icons.Default.Settings to R.string.settings,
    )
    val scope = rememberCoroutineScope()
    NavigationBar {
        items.forEachIndexed { page, (icon, text) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(stringResource(text)) },
                selected = pagerState.currentPage == page,
                onClick = {
                    if (pagerState.currentPage != page) {
                        scope.launch {
                            pagerState.scrollToPage(page)
//                        pagerState.animateScrollToPage(page)
                        }
                    }
                }
            )
        }
    }
}
