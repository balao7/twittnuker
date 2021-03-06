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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.text.BidiFormatter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.squareup.otto.Bus
import com.twitter.Validator
import nl.komponents.kovenant.Promise
import okhttp3.Dns
import org.mariotaku.restfu.http.RestHttpClient
import de.vanita5.twittnuker.fragment.iface.IBaseFragment
import de.vanita5.twittnuker.model.DefaultFeatures
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import de.vanita5.twittnuker.util.gifshare.GifShareProvider
import de.vanita5.twittnuker.util.premium.ExtraFeaturesService
import de.vanita5.twittnuker.util.schedule.StatusScheduleProvider
import de.vanita5.twittnuker.util.sync.SyncPreferences
import de.vanita5.twittnuker.util.sync.TimelineSyncManager
import javax.inject.Inject

open class BaseFragment : Fragment(), IBaseFragment<BaseFragment> {

    // Utility classes
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var bidiFormatter: BidiFormatter
    @Inject
    lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    lateinit var validator: Validator
    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
    @Inject
    lateinit var statusScheduleProviderFactory: StatusScheduleProvider.Factory
    @Inject
    lateinit var timelineSyncManagerFactory: TimelineSyncManager.Factory
    @Inject
    lateinit var gifShareProviderFactory: GifShareProvider.Factory
    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var dns: Dns
    @Inject
    lateinit var syncPreferences: SyncPreferences
    @Inject
    lateinit var externalThemeManager: ExternalThemeManager

    lateinit var requestManager: RequestManager
        private set

    protected val statusScheduleProvider: StatusScheduleProvider?
        get() = statusScheduleProviderFactory.newInstance(context)

    protected val timelineSyncManager: TimelineSyncManager?
        get() = timelineSyncManagerFactory.get()

    protected val gifShareProvider: GifShareProvider?
        get() = gifShareProviderFactory.newInstance(context)

    private val actionHelper = IBaseFragment.ActionHelper<BaseFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestManager = Glide.with(this)
    }

    override fun onStart() {
        super.onStart()
        requestManager.onStart()
    }

    override fun onStop() {
        requestManager.onStop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        actionHelper.dispatchOnResumeFragments(this)
    }

    override fun onPause() {
        actionHelper.dispatchOnPause()
        super.onPause()
    }

    override fun onDestroy() {
        requestManager.onDestroy()
        extraFeaturesService.release()
        super.onDestroy()
        DebugModeUtils.watchReferenceLeak(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestApplyInsets()
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (BaseFragment) -> Unit)
            : Promise<Unit, Exception> {
        return actionHelper.executeAfterFragmentResumed(this, useHandler, action)
    }

}