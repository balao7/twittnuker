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

package de.vanita5.twittnuker.fragment

import android.support.v4.app.FragmentManager
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.model.ParcelableUser

class ReportUserSpamDialogFragment : AbsUserMuteBlockDialogFragment() {
    override fun getMessage(user: ParcelableUser): String {
        val displayName = userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey])
        return getString(R.string.report_user_confirm_message, displayName)
    }

    override fun getTitle(user: ParcelableUser): String {
        val displayName = userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey])
        return getString(R.string.report_user, displayName)
    }

    override fun getPositiveButtonTitle(user: ParcelableUser): String {
        return getString(R.string.action_report_spam)
    }

    override fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean) {
        val accountKey = user.account_key ?: return
        twitterWrapper.reportSpamAsync(accountKey, user.key)
    }

    companion object {

        val FRAGMENT_TAG = "report_user_spam"

        fun show(fm: FragmentManager, user: ParcelableUser): ReportUserSpamDialogFragment {
            val f = ReportUserSpamDialogFragment()
            f.arguments = Bundle {
                this[EXTRA_USER] = user
            }
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}