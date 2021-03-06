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

package de.vanita5.twittnuker.fragment.statuses

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import com.squareup.otto.Subscribe
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.constant.userTimelineFilterKey
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.onShow
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.ParcelableStatusesFragment
import de.vanita5.twittnuker.loader.statuses.UserTimelineLoader
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.StatusPinEvent
import de.vanita5.twittnuker.model.timeline.TimelineFilter
import de.vanita5.twittnuker.model.timeline.UserTimelineFilter
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.view.holder.TimelineFilterHeaderViewHolder
import java.util.*

/**
 * User timeline
 */
class UserTimelineFragment : ParcelableStatusesFragment() {

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val accountKey = Utils.getAccountKey(context, arguments)
            val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            val result = ArrayList<String>()
            result.add(AUTHORITY_USER_TIMELINE)
            result.add("account=$accountKey")
            if (userKey != null) {
                result.add("user_id=$userKey")
            } else if (screenName != null) {
                result.add("screen_name=$screenName")
            } else {
                return null
            }
            (timelineFilter as? UserTimelineFilter)?.let {
                if (it.isIncludeReplies) {
                    result.add("include_replies")
                }
                if (it.isIncludeRetweets) {
                    result.add("include_retweets")
                }
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            if (arguments.getLong(EXTRA_TAB_ID, -1) < 0) return null
            val sb = StringBuilder("user_timeline_")

            val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            if (userKey != null) {
                sb.append(userKey)
            } else if (screenName != null) {
                sb.append(screenName)
            } else {
                return null
            }
            return sb.toString()
        }

    override val enableTimelineFilter: Boolean
        get() = arguments.getBoolean(EXTRA_ENABLE_TIMELINE_FILTER)

    override val timelineFilter: TimelineFilter?
        get() = if (enableTimelineFilter) preferences[userTimelineFilterKey] else null

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val data = adapterData
        val accountKey = Utils.getAccountKey(context, args)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val profileUrl = args.getString(EXTRA_PROFILE_URL)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        val loadPinnedStatus = args.getBoolean(EXTRA_LOAD_PINNED_STATUS, false)
        return UserTimelineLoader(context, accountKey, userKey, screenName, profileUrl, data,
                savedStatusesFileArgs, tabPosition, fromUser, loadingMore, loadPinnedStatus,
                timelineFilter as? UserTimelineFilter)
    }

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        loader as UserTimelineLoader
        if (loader.loadPinnedStatus) {
            adapter.pinnedStatuses = loader.pinnedStatuses
        }
        super.onStatusesLoaded(loader, data)
    }

    override fun onFilterClick(holder: TimelineFilterHeaderViewHolder) {
        val df = UserTimelineFilterDialogFragment()
        df.setTargetFragment(this, REQUEST_SET_TIMELINE_FILTER)
        df.show(fragmentManager, "set_timeline_filter")
    }

    @Subscribe
    fun onStatusPinEvent(event: StatusPinEvent) {
        val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY) ?: return
        if (event.userKey != userKey) return
        triggerRefresh()
    }

    private fun reloadAllStatuses() {
        adapterData = null
        triggerRefresh()
        showProgress()
    }

    class UserTimelineFilterDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            val values = resources.getStringArray(R.array.values_user_timeline_filter)
            val checkedItems = BooleanArray(values.size) {
                val filter = preferences[userTimelineFilterKey]
                when (values[it]) {
                    "replies" -> filter.isIncludeReplies
                    "retweets" -> filter.isIncludeRetweets
                    else -> false
                }
            }
            builder.setTitle(R.string.title_user_timeline_filter)
            builder.setMultiChoiceItems(R.array.entries_user_timeline_filter, checkedItems, null)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog as AlertDialog
                val listView = dialog.listView
                val filter = UserTimelineFilter().apply {
                    isIncludeRetweets = listView.isItemChecked(values.indexOf("retweets"))
                    isIncludeReplies = listView.isItemChecked(values.indexOf("replies"))
                }
                preferences.edit().apply {
                    this[userTimelineFilterKey] = filter
                }.apply()
                (targetFragment as UserTimelineFragment).reloadAllStatuses()
            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    companion object {
        const val EXTRA_ENABLE_TIMELINE_FILTER = "enable_timeline_filter"
        const val EXTRA_LOAD_PINNED_STATUS = "load_pinned_status"
        const val REQUEST_SET_TIMELINE_FILTER = 101
    }
}