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

package de.vanita5.twittnuker.loader.users

import android.content.Context
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.exception.APINotSupportedException
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedArrayList
import de.vanita5.twittnuker.model.pagination.PaginatedList

class StatusRetweetersLoader(
        context: Context,
        accountKey: UserKey?,
        private val statusId: String,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : AbsRequestUsersLoader(context, accountKey, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        when (details.type) {
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                val response = mastodon.getStatusFavouritedBy(statusId)
                return PaginatedArrayList<ParcelableUser>(response.size).apply {
                    response.mapTo(this) { account ->
                        account.toParcelable(details)
                    }
                }
            }
            AccountType.TWITTER -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                val ids = microBlog.getRetweetersIDs(statusId, paging).iDs
                val response = microBlog.lookupUsers(ids)
                return PaginatedArrayList<ParcelableUser>(response.size).apply {
                    response.mapTo(this) { user ->
                        user.toParcelable(details, profileImageSize = profileImageSize)
                    }
                }
            }
            else -> {
                throw APINotSupportedException(details.type)
            }
        }
    }

}