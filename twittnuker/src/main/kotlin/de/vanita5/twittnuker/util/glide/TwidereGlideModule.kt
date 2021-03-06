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

package de.vanita5.twittnuker.util.glide

import android.content.Context
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import okhttp3.OkHttpClient
import okhttp3.Request
import de.vanita5.twittnuker.model.media.AuthenticatedUri
import de.vanita5.twittnuker.util.HttpClientFactory
import de.vanita5.twittnuker.util.UserAgentUtils
import de.vanita5.twittnuker.util.dagger.DependencyHolder
import de.vanita5.twittnuker.util.okhttp.ModifyRequestInterceptor
import java.io.InputStream

class TwidereGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Do nothing.
    }

    override fun registerComponents(context: Context, glide: Glide) {
        val holder = DependencyHolder.get(context)
        val builder = OkHttpClient.Builder()
        val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
        HttpClientFactory.initOkHttpClient(conf, builder, holder.dns, holder.connectionPool, holder.cache)
        val userAgent = try {
            UserAgentUtils.getDefaultUserAgentStringSafe(context)
        } catch (e: Exception) {
            null
        }
        builder.addInterceptor(ModifyRequestInterceptor(UserAgentModifier(userAgent)))
        val client = builder.build()
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
        glide.register(AuthenticatedUri::class.java, InputStream::class.java, AuthenticatedUriLoader.Factory(client))
    }

    class UserAgentModifier(val userAgent: String?) : ModifyRequestInterceptor.RequestModifier {

        override fun modify(original: Request, builder: Request.Builder): Boolean {
            if (userAgent != null) {
                builder.header("User-Agent", userAgent)
            }
            return true
        }

    }
}
