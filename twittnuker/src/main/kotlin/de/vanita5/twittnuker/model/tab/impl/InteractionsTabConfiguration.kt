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

package de.vanita5.twittnuker.model.tab.impl

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.TabAccountFlags
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_MENTIONS_ONLY
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_MY_FOLLOWING_ONLY
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.fragment.InteractionsTimelineFragment
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.Tab
import de.vanita5.twittnuker.model.tab.BooleanHolder
import de.vanita5.twittnuker.model.tab.DrawableHolder
import de.vanita5.twittnuker.model.tab.StringHolder
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.model.tab.conf.BooleanExtraConfiguration
import de.vanita5.twittnuker.model.tab.extra.InteractionsTabExtras
import de.vanita5.twittnuker.model.util.AccountUtils


class InteractionsTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.interactions)

    override val icon = DrawableHolder.Builtin.NOTIFICATIONS

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_MULTIPLE or TabAccountFlags.FLAG_ACCOUNT_MUTABLE

    override val fragmentClass = InteractionsTimelineFragment::class.java

    override fun getExtraConfigurations(context: Context) = arrayOf(
            BooleanExtraConfiguration(EXTRA_MY_FOLLOWING_ONLY, R.string.following_only, false).mutable(true),
            MentionsOnlyExtraConfiguration(EXTRA_MENTIONS_ONLY).mutable(true)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as InteractionsTabExtras
        when (extraConf.key) {
            EXTRA_MY_FOLLOWING_ONLY -> {
                extras.isMyFollowingOnly = (extraConf as BooleanExtraConfiguration).value
            }
            EXTRA_MENTIONS_ONLY -> {
                extras.isMentionsOnly = (extraConf as BooleanExtraConfiguration).value
            }
        }
        return true
    }

    override fun readExtraConfigurationFrom(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as? InteractionsTabExtras ?: return false
        when (extraConf.key) {
            EXTRA_MY_FOLLOWING_ONLY -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isMyFollowingOnly
            }
            EXTRA_MENTIONS_ONLY -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isMentionsOnly
            }
        }
        return true
    }

    private class MentionsOnlyExtraConfiguration(key: String) : BooleanExtraConfiguration(key,
            StringHolder.resource(R.string.mentions_only),
            MentionsOnlyExtraConfiguration.HasOfficialBooleanHolder()) {

        private var valueBackup: Boolean = false

        override fun onAccountSelectionChanged(account: AccountDetails?) {
            val hasOfficial: Boolean
            if (account == null || account.dummy) {
                hasOfficial = AccountUtils.hasOfficialKeyAccount(context)
            } else {
                hasOfficial = account.isOfficial(context)
            }
            (defaultValue as HasOfficialBooleanHolder).hasOfficial = hasOfficial
            val checkBox = view.findViewById(android.R.id.checkbox) as CheckBox
            val titleView = view.findViewById(android.R.id.title) as TextView
            val summaryView = view.findViewById(android.R.id.summary) as TextView
            view.isEnabled = hasOfficial
            titleView.isEnabled = hasOfficial
            summaryView.isEnabled = hasOfficial
            checkBox.isEnabled = hasOfficial
            if (hasOfficial) {
                checkBox.isChecked = valueBackup
                summaryView.visibility = View.GONE
            } else {
                valueBackup = checkBox.isChecked
                checkBox.isChecked = true
                summaryView.setText(R.string.summary_interactions_not_available)
                summaryView.visibility = View.VISIBLE
            }
        }

        override var value: Boolean
            get() {
                if ((defaultValue as HasOfficialBooleanHolder).hasOfficial) {
                    return super.value
                }
                return valueBackup
            }
            set(value) {
                super.value = value
            }

        private class HasOfficialBooleanHolder : BooleanHolder() {

            var hasOfficial: Boolean = false

            override fun createBoolean(context: Context): Boolean {
                return hasOfficial
            }

        }
    }

}