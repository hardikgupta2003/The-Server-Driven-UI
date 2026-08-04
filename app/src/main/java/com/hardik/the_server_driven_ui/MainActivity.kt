package com.hardik.the_server_driven_ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hardik.the_server_driven_ui.sdui.action.ActionDispatcher
import com.hardik.the_server_driven_ui.sdui.action.buildNodeIndex
import com.hardik.the_server_driven_ui.sdui.loader.JsonPageLoader
import com.hardik.the_server_driven_ui.sdui.renderer.RenderNode
import com.hardik.the_server_driven_ui.sdui.renderer.SduiPage
import com.hardik.the_server_driven_ui.sdui.renderer.buildDefaultRegistry
import com.hardik.the_server_driven_ui.sdui.state.SduiStateStore
import com.hardik.the_server_driven_ui.ui.theme.TheServerDrivenUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheServerDrivenUITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SduiHomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Entry point for the SDUI-rendered landing page. Everything below the
 * asset load is generic — swapping `landing_page.json` for a different
 * Cars24 screen's payload needs no changes here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SduiHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val stateStore: SduiStateStore = viewModel()

    val pageSchema = remember { JsonPageLoader.loadFromAssets(context, "sdui/landing_page.json") }
    val registry = remember { buildDefaultRegistry() }
    val nodeIndex = remember(pageSchema) { buildNodeIndex(pageSchema.page.sections) }
    val actionDispatcher = remember(nodeIndex) {
        ActionDispatcher(
            stateStore = stateStore,
            nodeIndex = nodeIndex,
            onNavigate = { route, params ->
                Toast.makeText(context, "navigate → $route $params", Toast.LENGTH_SHORT).show()
            },
        )
    }

    SduiPage(
        page = pageSchema.page,
        registry = registry,
        stateStore = stateStore,
        actionDispatcher = actionDispatcher,
        modifier = modifier,
    )

    val sheetNode by stateStore.sheetContent.collectAsState()
    val currentState by stateStore.values.collectAsState()
    sheetNode?.let { node ->
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { stateStore.closeSheet() },
            sheetState = sheetState,
        ) {
            RenderNode(
                node = node,
                registry = registry,
                currentState = currentState,
                actionDispatcher = actionDispatcher,
            )
        }
    }
}
