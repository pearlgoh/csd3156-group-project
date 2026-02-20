package com.csd3156.game

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
//import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.csd3156.game.ui.theme.GameTheme
import androidx.activity.viewModels
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val gameView by viewModels<GameViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTheme {
                //setContentView(GameView(this)) // temp
                //GameApp()
                Nav(Modifier.padding(32.dp).fillMaxSize(), this, gameView)
            }
        }
    }
}

data object GameView

@Composable
fun Nav(mod: Modifier, ctx: Context, gameView: GameViewModel) {
    val viewModelFactory = MainViewModelFactory(/*(application as App).repository*/)

    val backstack = remember { mutableStateListOf<Any>(GameView) }
    val viewModel = viewModel<MainViewModel>(factory = viewModelFactory)

    var currBot by remember { mutableStateOf("") }
    var currBotId by remember { mutableStateOf(0) }

    var chatBotsOrder by remember { mutableStateOf(MutableList(50) {index -> index}) }

    NavDisplay(
        backStack = backstack,
        onBack = {
            backstack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = {
                key -> when(key) {
            is GameView -> NavEntry(key) {
                GameScreen(modifier = mod, context = ctx, viewModel = gameView)
            }

            else -> NavEntry(key) { Text("") }
        }
        }
    )
}

@PreviewScreenSizes
@Composable
fun GameApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

//    NavigationSuiteScaffold(
//        navigationSuiteItems = {
//            AppDestinations.entries.forEach {
//                item(
//                    icon = {
//                        Icon(
//                            it.icon,
//                            contentDescription = it.label
//                        )
//                    },
//                    label = { Text(it.label) },
//                    selected = it == currentDestination,
//                    onClick = { currentDestination = it }
//                )
//            }
//        }
//    ) {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            Greeting(
//                name = "Android",
//                modifier = Modifier.padding(innerPadding)
//            )
//        }
//    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GameTheme {
        Greeting("Android")
    }
}