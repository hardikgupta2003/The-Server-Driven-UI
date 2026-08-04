package com.hardik.the_server_driven_ui.sdui.state

import androidx.lifecycle.ViewModel
import com.hardik.the_server_driven_ui.sdui.model.SduiNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shared client-side state the page's SduiActions read/write. Backs
 * DataBinding resolution (chip selection -> which rail variant renders)
 * and the currently-open bottom sheet, if any.
 */
class SduiStateStore : ViewModel() {

    private val _values = MutableStateFlow<Map<String, String>>(emptyMap())
    val values: StateFlow<Map<String, String>> = _values.asStateFlow()

    private val _sheetContent = MutableStateFlow<SduiNode?>(null)
    val sheetContent: StateFlow<SduiNode?> = _sheetContent.asStateFlow()

    fun update(key: String, value: String) {
        _values.update { it + (key to value) }
    }

    fun get(key: String): String? = _values.value[key]

    fun openSheet(node: SduiNode?) {
        _sheetContent.value = node
    }

    fun closeSheet() {
        _sheetContent.value = null
    }
}
