package com.novatune.app.lyrics

import android.content.Context
import com.novatune.app.constants.EnableKugouKey
import com.novatune.app.utils.dataStore
import com.novatune.app.utils.get
import com.zionhuang.kugou.KuGou

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        KuGou.getAllPossibleLyricsOptions(title, artist, duration, callback)
    }
}
