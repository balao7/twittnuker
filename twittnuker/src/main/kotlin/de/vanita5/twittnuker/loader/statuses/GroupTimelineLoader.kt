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

package de.vanita5.twittnuker.loader.statuses

import android.content.Context
import android.support.annotation.WorkerThread
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.twitter.model.Paging
import de.vanita5.microblog.library.twitter.model.Status
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.FilterScope
import de.vanita5.twittnuker.exception.APINotSupportedException
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.util.database.ContentFiltersUtils

class GroupTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        private val groupId: String?,
        private val groupName: String?,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, adapterData, savedStatusesArgs, tabPosition,
        fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        if (account.type != AccountType.STATUSNET) throw APINotSupportedException()
        return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
            it.toParcelable(account, profileImageSize)
        }
    }
    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, true,
                FilterScope.LIST_GROUP_TIMELINE)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        return when {
            groupId != null -> {
                microBlog.getGroupStatuses(groupId, paging)
            }
            groupName != null -> {
                microBlog.getGroupStatusesByName(groupName, paging)
            }
            else -> {
                throw MicroBlogException("No group name or id given")
            }
        }
    }

}