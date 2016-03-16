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

package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.SearchQuery;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.Nullables;
import de.vanita5.twittnuker.util.ParcelUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConversationLoader extends TwitterAPIStatusesLoader {

    @NonNull
    private final ParcelableStatus mStatus;
    private final long mSinceSortId, mMaxSortId;
    private boolean mCanLoadAllReplies;

    public ConversationLoader(final Context context, @NonNull final ParcelableStatus status,
                              final String sinceId, final String maxId,
                              final long sinceSortId, final long maxSortId,
                              final List<ParcelableStatus> data, final boolean fromUser,
                              final boolean loadingMore) {
        super(context, status.account_key, sinceId, maxId, data, null, -1, fromUser, loadingMore);
        mStatus = Nullables.assertNonNull(ParcelUtils.clone(status));
        mSinceSortId = sinceSortId;
        mMaxSortId = maxSortId;
        ParcelableStatusUtils.makeOriginalStatus(mStatus);
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter,
                                    @NonNull final ParcelableCredentials credentials,
                                    @NonNull final Paging paging) throws TwitterException {
        mCanLoadAllReplies = false;
        final ParcelableStatus status = mStatus;
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                final boolean isOfficial = Utils.isOfficialCredentials(getContext(), credentials);
                mCanLoadAllReplies = isOfficial;
                if (isOfficial) {
                    return twitter.showConversation(status.id, paging);
                } else {
                    return showConversationCompat(twitter, credentials, status, true);
                }
            }
            case ParcelableAccount.Type.STATUSNET: {
                mCanLoadAllReplies = true;
                return twitter.getStatusNetConversation(status.id, paging);
            }
            case ParcelableAccount.Type.FANFOU: {
                mCanLoadAllReplies = true;
                return twitter.getContextTimeline(status.id, paging);
            }
        }
        // Set to true because there's no conversation support on this platform
        mCanLoadAllReplies = true;
        return showConversationCompat(twitter, credentials, status, false);
    }

    protected List<Status> showConversationCompat(@NonNull final Twitter twitter,
                                                  @NonNull final ParcelableCredentials credentials,
                                                  @NonNull final ParcelableStatus status,
                                                  final boolean loadReplies) throws TwitterException {
        final List<Status> statuses = new ArrayList<>();
        final String maxId = getMaxId(), sinceId = getSinceId();
        final long maxSortId = getMaxSortId(), sinceSortId = getSinceSortId();
        final boolean noSinceMaxId = maxId == null && sinceId == null;
        // Load conversations
        if ((maxId != null && maxSortId < status.sort_id) || noSinceMaxId) {
            String inReplyToId = maxId != null ? maxId : status.in_reply_to_status_id;
            int count = 0;
            while (inReplyToId != null && count < 10) {
                final Status item = twitter.showStatus(inReplyToId);
                inReplyToId = item.getInReplyToStatusId();
                statuses.add(item);
                count++;
            }
        }
        if (loadReplies) {
            // Load replies
            if ((sinceId != null && sinceSortId > status.sort_id) || noSinceMaxId) {
                SearchQuery query = new SearchQuery();
                if (TwitterAPIFactory.isTwitterCredentials(credentials)) {
                    query.query("to:" + status.user_screen_name);
                } else {
                    query.query("@" + status.user_screen_name);
                }
                query.sinceId(sinceId != null ? sinceId : status.id);
                try {
                    for (Status item : twitter.search(query)) {
                        if (TextUtils.equals(item.getInReplyToStatusId(), status.id)) {
                            statuses.add(item);
                        }
                    }
                } catch (TwitterException e) {
                    // Ignore for now
                }
            }
        }
        return statuses;
    }

    public boolean canLoadAllReplies() {
        return mCanLoadAllReplies;
    }

    public long getSinceSortId() {
        return mSinceSortId;
    }

    public long getMaxSortId() {
        return mMaxSortId;
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(SQLiteDatabase database, ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, status, false);
    }

}