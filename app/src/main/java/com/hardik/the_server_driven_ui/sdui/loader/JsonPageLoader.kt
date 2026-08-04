package com.hardik.the_server_driven_ui.sdui.loader

import android.content.Context
import com.hardik.the_server_driven_ui.perf.PerfTrace
import com.hardik.the_server_driven_ui.sdui.model.PageSchema
import kotlinx.serialization.json.Json

/**
 * Mock "server": reads the page payload straight out of assets. Swapping
 * this for a real network call later is a one-function change — nothing
 * downstream (renderer, registry, state) knows or cares where the JSON
 * came from.
 */
object JsonPageLoader {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun loadFromAssets(context: Context, fileName: String): PageSchema {
        val readStart = System.nanoTime()
        val raw = context.assets.open(fileName).bufferedReader().use { it.readText() }
        PerfTrace.duration("json_read", readStart)

        val parseStart = System.nanoTime()
        val schema = json.decodeFromString(PageSchema.serializer(), raw)
        PerfTrace.duration("json_parse", parseStart)

        return schema
    }

    fun loadFromString(raw: String): PageSchema =
        json.decodeFromString(PageSchema.serializer(), raw)
}
