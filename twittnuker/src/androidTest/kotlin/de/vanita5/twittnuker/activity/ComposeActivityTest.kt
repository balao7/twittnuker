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

package de.vanita5.twittnuker.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import kotlinx.android.synthetic.main.activity_compose.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.ParcelableStatusUpdate
import de.vanita5.twittnuker.test.R
import de.vanita5.twittnuker.util.getJsonResource

@RunWith(AndroidJUnit4::class)
@SuppressLint("SetTextI18n")
class ComposeActivityTest {

    @get:Rule
    val activityRule = ComposeActivityTestRule(launchActivity = false)

    @Test
    fun testReply() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_848051071444410368)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("@t_deyarmin @nixcraft @mariotaku Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        assertExcludedMatches(emptyArray(), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    @Test
    fun testReplyRemovedSomeMentions() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_848051071444410368)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("@t_deyarmin Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        assertExcludedMatches(arrayOf("17484680", "57610574"), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    @Test
    fun testReplyNoMentions() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_848051071444410368)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        Assert.assertEquals("https://twitter.com/t_deyarmin/status/847950697987493888",
                statusUpdate.attachment_url)
        assertExcludedMatches(emptyArray(), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    @Test
    fun testReplySelf() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_852737226718838790)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("@TwidereProject @mariotaku Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        assertExcludedMatches(emptyArray(), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    @Test
    fun testReplySelfRemovedSomeMentions() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_852737226718838790)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("@TwidereProject Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        assertExcludedMatches(arrayOf("57610574"), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    @Test
    fun testReplySelfNoMentions() {
        val context = InstrumentationRegistry.getContext()
        val targetContext = InstrumentationRegistry.getTargetContext()
        val status: ParcelableStatus = context.resources.getJsonResource(R.raw.parcelable_status_852737226718838790)
        val intent = Intent(INTENT_ACTION_REPLY)
        intent.setClass(targetContext, ComposeActivity::class.java)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_SAVE_DRAFT, true)
        val activity = activityRule.launchActivity(intent)
        activityRule.runOnUiThread {
            activity.editText.setText("Test Reply")
        }
        val statusUpdate = activity.getStatusUpdateTest(false)
        Assert.assertEquals("Test Reply", statusUpdate.text)
        assertExcludedMatches(arrayOf("583328497", "57610574"), statusUpdate)
        activity.requestSkipDraft()
        activity.finish()
    }

    private fun Activity.getStatusUpdateTest(checkLength: Boolean): ParcelableStatusUpdate {
        val getStatusUpdate = javaClass.getDeclaredMethod("getStatusUpdate",
                kotlin.Boolean::class.java).apply {
            isAccessible = true
        }
        return getStatusUpdate(this, checkLength) as ParcelableStatusUpdate
    }

    private fun assertExcludedMatches(expectedIds: Array<String>, statusUpdate: ParcelableStatusUpdate): Boolean {
        return statusUpdate.excluded_reply_user_ids?.all { excludedId ->
            expectedIds.any { expectation ->
                expectation.equals(excludedId, ignoreCase = true)
            }
        } ?: expectedIds.isEmpty()
    }
}