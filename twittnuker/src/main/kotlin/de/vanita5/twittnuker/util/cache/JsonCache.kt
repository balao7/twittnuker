/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.util.cache

import com.bluelinelabs.logansquare.LoganSquare
import com.bumptech.glide.disklrucache.DiskLruCache
import de.vanita5.twittnuker.BuildConfig
import java.io.File
import java.io.IOException


class JsonCache(val cacheDir: File) {

    private val cache = try {
        DiskLruCache.open(cacheDir, BuildConfig.VERSION_CODE, 1, 100 * 1048576)
    } catch (e: IOException) {
        null
    }

    fun <T> getList(key: String, cls: Class<T>): List<T>? {
        return cache?.get(key)?.getFile(0)?.inputStream()?.use {
            LoganSquare.parseList(it, cls)
        }
    }

    fun <T> saveList(key: String, list: List<T>, cls: Class<T>) {
        val editor = cache?.edit(key) ?: return
        try {
            editor.getFile(0)?.outputStream()?.use {
                LoganSquare.serialize(list, it, cls)
            }
            editor.commit()
        } finally {
            editor.abortUnlessCommitted()
        }
    }
}