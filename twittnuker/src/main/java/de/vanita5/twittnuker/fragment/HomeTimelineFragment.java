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

package de.vanita5.twittnuker.fragment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.annotation.ReadPositionTag;
import de.vanita5.twittnuker.model.RefreshTaskParam;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ErrorInfoStore;

public class HomeTimelineFragment extends CursorStatusesFragment {

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_HOME_TIMELINE;
    }

    @Override
    public Uri getContentUri() {
        return Statuses.CONTENT_URI;
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_HOME_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }

    @Override
    protected void updateRefreshState() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter == null) return;
        setRefreshing(twitter.isHomeTimelineRefreshing());
    }

    @Override
    public boolean isRefreshing() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        return twitter != null && twitter.isHomeTimelineRefreshing();
    }

    @Override
    public boolean getStatuses(RefreshTaskParam param) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter == null) return false;
        if (!param.hasMaxIds()) return twitter.refreshAll(param.getAccountKeys());
        return twitter.getHomeTimelineAsync(param);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        final Context context = getContext();
        if (isVisibleToUser && context != null) {
            for (UserKey accountId : getAccountKeys()) {
                final String tag = "home_" + accountId;
                mNotificationManager.cancel(tag, NOTIFICATION_ID_HOME_TIMELINE);
            }
        }
    }

    @Override
    @ReadPositionTag
    protected String getReadPositionTag() {
        return ReadPositionTag.HOME_TIMELINE;
    }

}