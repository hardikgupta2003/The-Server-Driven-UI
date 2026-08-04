package com.hardik.the_server_driven_ui.sdui.renderer

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hardik.the_server_driven_ui.BuildConfig
import com.hardik.the_server_driven_ui.sdui.model.SduiNode

private const val TAG = "SduiUnknownComponent"

/**
 * The registry-miss fallback: a server-sent `type` the client has no
 * composable for. This is the mechanism that makes the whole page
 * degrade gracefully instead of crashing — logged once, then either
 * invisible (release) or a visible marker (debug) so it's demonstrable.
 */
val unknownComponent: SduiComponent = { node, _ ->
    Log.w(TAG, "Unrecognized component type='${node.type}' id='${node.id}'. Skipping — page continues.")
    if (BuildConfig.DEBUG) {
        UnknownComponentDebugPlaceholder(node)
    }
}

@Composable
private fun UnknownComponentDebugPlaceholder(node: SduiNode) {
    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = "⚠ Unknown component\ntype: \"${node.type}\"\nid: \"${node.id}\"",
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
