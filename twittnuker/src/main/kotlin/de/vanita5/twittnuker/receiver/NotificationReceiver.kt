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

package de.vanita5.twittnuker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.toLongOr
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.NotificationType
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.constant.IntentConstants.BROADCAST_NOTIFICATION_DELETED
import de.vanita5.twittnuker.constant.promotionsEnabledKey
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.task.twitter.message.BatchMarkMessageReadTask
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.dagger.DependencyHolder

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BROADCAST_NOTIFICATION_DELETED -> {
                handleNotificationDeleted(intent, context)
            }
            BROADCAST_PROMOTIONS_ACCEPTED -> {
                setPromotionsEnabled(context, intent, true)
            }
            BROADCAST_PROMOTIONS_DENIED -> {
                setPromotionsEnabled(context, intent, false)
            }
        }
    }

    private fun handleNotificationDeleted(intent: Intent, context: Context) {
        val uri = intent.data ?: return
        val holder = DependencyHolder.get(context)
        @NotificationType
        val notificationType = uri.getQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE)
        val accountKey = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY)?.let(UserKey::valueOf)
        val paramReadPosition = uri.getQueryParameter(QUERY_PARAM_READ_POSITION)
        when (notificationType) {
            NotificationType.HOME_TIMELINE -> {
                val positionTag = Utils.getReadPositionTagWithAccount(ReadPositionTag.HOME_TIMELINE,
                        accountKey)
                val manager = holder.readStateManager
                manager.setPosition(positionTag, paramReadPosition.toLongOr(-1L))
            }
            NotificationType.INTERACTIONS -> {
                val positionTag = Utils.getReadPositionTagWithAccount(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                        accountKey)
                val manager = holder.readStateManager
                manager.setPosition(positionTag, paramReadPosition.toLongOr(-1L))
            }
            NotificationType.DIRECT_MESSAGES -> {
                if (accountKey == null) return
                val appContext = context.applicationContext
                val task = BatchMarkMessageReadTask(appContext, accountKey,
                        paramReadPosition.toLongOr(-1L))
                TaskStarter.execute(task)
            }
        }
    }

    private fun setPromotionsEnabled(context: Context, intent: Intent, enabled: Boolean) {
        val holder = DependencyHolder.get(context)
        holder.preferences[promotionsEnabledKey] = enabled
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            holder.notificationManager.cancel(notificationId)
        }
    }
}