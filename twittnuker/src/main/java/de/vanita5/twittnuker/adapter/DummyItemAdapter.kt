/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.support.v4.text.BidiFormatter
import android.support.v7.widget.RecyclerView

import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.adapter.iface.*
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

import javax.inject.Inject

class DummyItemAdapter @JvmOverloads constructor(
        override val context: Context,
        override val twidereLinkify: TwidereLinkify = TwidereLinkify(null),
        private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null
) : IStatusesAdapter<Any>, IUsersAdapter<Any>, IUserListsAdapter<Any>, SharedPreferenceConstants {

    private val preferences: SharedPreferencesWrapper
    override val mediaLoadingHandler: MediaLoadingHandler
    @Inject
    override lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    override lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    override lateinit var bidiFormatter: BidiFormatter

    override var profileImageStyle: Int = 0
    override var mediaPreviewStyle: Int = 0
    override var textSize: Float = 0f
    override var linkHighlightingStyle: Int = 0
    override var nameFirst: Boolean = false
    override var profileImageEnabled: Boolean = false
    override var sensitiveContentEnabled: Boolean = false
    override var mediaPreviewEnabled: Boolean = false
    override var isShowAbsoluteTime: Boolean = false
    override var followClickListener: IUsersAdapter.FollowClickListener? = null
    override var requestClickListener: IUsersAdapter.RequestClickListener? = null
    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null
    override var userClickListener: IUsersAdapter.UserClickListener? = null
    override var showAccountsColor: Boolean = false
    override var useStarsForLikes: Boolean = false

    private var showCardActions: Boolean = false
    private var showingActionCardPosition = RecyclerView.NO_POSITION

    init {
        GeneralComponentHelper.build(context).inject(this)
        preferences = SharedPreferencesWrapper.getInstance(context, TwittnukerConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        mediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)
        updateOptions()
    }

    fun setShouldShowAccountsColor(shouldShowAccountsColor: Boolean) {
        this.showAccountsColor = shouldShowAccountsColor
    }


    override fun getItemCount(): Int {
        return 0
    }

    override var loadMoreIndicatorPosition: Long
        @IndicatorPosition
        get() = ILoadMoreSupportAdapter.NONE
        set(@IndicatorPosition position) {

    }

    override var loadMoreSupportedPosition: Long
        @IndicatorPosition
        get() = ILoadMoreSupportAdapter.NONE
        set(@IndicatorPosition supported) {

    }

    override fun getStatus(position: Int): ParcelableStatus? {
        if (adapter is ParcelableStatusesAdapter) {
            return adapter.getStatus(position)
        } else if (adapter is VariousItemsAdapter) {
            return adapter.getItem(position) as ParcelableStatus
        }
        return null
    }

    override val statusCount: Int
        get() = 0

    override val rawStatusCount: Int
        get() = 0

    override fun getStatusId(position: Int): String? {
        return null
    }

    override fun getStatusTimestamp(position: Int): Long {
        return -1
    }

    override fun getStatusPositionKey(position: Int): Long {
        return -1
    }

    override fun getAccountKey(position: Int): UserKey? {
        return null
    }

    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        return null
    }

    override fun isCardActionsShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardActions
        return showCardActions || showingActionCardPosition == position
    }

    override fun showCardActions(position: Int) {
        if (showingActionCardPosition != RecyclerView.NO_POSITION && adapter != null) {
            adapter.notifyItemChanged(showingActionCardPosition)
        }
        showingActionCardPosition = position
        if (position != RecyclerView.NO_POSITION && adapter != null) {
            adapter.notifyItemChanged(position)
        }
    }

    override fun getUser(position: Int): ParcelableUser? {
        if (adapter is ParcelableUsersAdapter) {
            return adapter.getUser(position)
        } else if (adapter is VariousItemsAdapter) {
            return adapter.getItem(position) as ParcelableUser
        }
        return null
    }

    override val userCount: Int
        get() = 0

    override val userListsCount: Int
        get() = 0

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = null
    override val userListClickListener: IUserListsAdapter.UserListClickListener?
        get() = null

    override fun getUserId(position: Int): String? {
        return null
    }

    override fun getUserList(position: Int): ParcelableUserList? {
        return null
    }

    override fun getUserListId(position: Int): String? {
        return null
    }

    override fun setData(data: Any?): Boolean {
        return false
    }

    override fun isGapItem(position: Int): Boolean {
        return false
    }

    fun updateOptions() {
        profileImageStyle = Utils.getProfileImageStyle(preferences.getString(SharedPreferenceConstants.KEY_PROFILE_IMAGE_STYLE, null))
        mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(SharedPreferenceConstants.KEY_MEDIA_PREVIEW_STYLE, null))
        textSize = preferences.getInt(SharedPreferenceConstants.KEY_TEXT_SIZE, context.resources.getInteger(R.integer.default_text_size)).toFloat()
        nameFirst = preferences.getBoolean(SharedPreferenceConstants.KEY_NAME_FIRST, true)
        profileImageEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true)
        mediaPreviewEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_MEDIA_PREVIEW, false)
        sensitiveContentEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS, false)
        showCardActions = !preferences.getBoolean(SharedPreferenceConstants.KEY_HIDE_CARD_ACTIONS, false)
        linkHighlightingStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(SharedPreferenceConstants.KEY_LINK_HIGHLIGHT_OPTION, null))
        useStarsForLikes = preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK)
        isShowAbsoluteTime = preferences.getBoolean(SharedPreferenceConstants.KEY_SHOW_ABSOLUTE_TIME)
    }
}