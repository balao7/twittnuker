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
import org.mariotaku.ktextension.isNullOrEmpty
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.microblog.library.twitter.model.*
import de.vanita5.twittnuker.alias.MastodonTimelineOption
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.FilterScope
import de.vanita5.twittnuker.extension.api.tryShowUser
import de.vanita5.twittnuker.extension.model.api.mastodon.mapToPaginated
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.api.updateFilterInfoForUserTimeline
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.PaginatedList
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.database.ContentFiltersUtils

class MediaTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, data, savedStatusesArgs, tabPosition, fromUser, loadingMore) {

    private var user: User? = null

    private val isMyTimeline: Boolean
        get() {
            val accountKey = accountKey ?: return false
            if (userKey != null) {
                return userKey.maybeEquals(accountKey)
            } else {
                val accountScreenName = DataStoreUtils.getAccountScreenName(context, accountKey)
                return accountScreenName != null && accountScreenName.equals(screenName, ignoreCase = true)
            }
        }

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        return when (account.type) {
            AccountType.MASTODON -> getMastodonStatuses(account, paging)
            else -> getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
                it.toParcelable(account, profileImageSize = profileImageSize,
                        updateFilterInfoAction = ::updateFilterInfoForUserTimeline)
            }
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        if (status.media.isNullOrEmpty()) return false
        return !isMyTimeline && ContentFiltersUtils.isFiltered(context.contentResolver, status,
                true, FilterScope.USER_TIMELINE)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): ResponseList<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    if (userKey != null) {
                        return microBlog.getMediaTimeline(userKey.id, paging)
                    }
                    if (screenName != null) {
                        return microBlog.getMediaTimelineByScreenName(screenName, paging)
                    }
                } else {
                    val screenName = this.screenName ?: run {
                        return@run this.user ?: run fetchUser@ {
                            if (userKey == null) throw MicroBlogException("Invalid parameters")
                            val user = microBlog.tryShowUser(userKey.id, null, account.type)
                            this.user = user
                            return@fetchUser user
                        }.screenName
                    }
                    val query = SearchQuery("from:$screenName filter:media exclude:retweets")
                    query.paging(paging)
                    val result = ResponseList<Status>()
                    microBlog.search(query).filterTo(result) { status ->
                        val user = status.user
                        return@filterTo user.id == userKey?.id
                                || user.screenName.equals(this.screenName, ignoreCase = true)
                    }
                    return result
                }
                throw MicroBlogException("Wrong user")
            }
            AccountType.FANFOU -> {
                if (userKey != null) {
                    return microBlog.getPhotosUserTimeline(userKey.id, paging)
                }
                if (screenName != null) {
                    return microBlog.getPhotosUserTimeline(screenName, paging)
                }
                throw MicroBlogException("Wrong user")
            }
        }
        throw MicroBlogException("Not implemented")
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        val option = MastodonTimelineOption()
        option.onlyMedia(true)
        return UserTimelineLoader.getMastodonStatuses(mastodon, userKey, screenName, paging,
                option).mapToPaginated { it.toParcelable(account) }
    }
}