/*
 *          Twittnuker - Twitter client for Android
 *
 *  Copyright 2013-2017 vanita5 <mail@vanit.as>
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 *  Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.vanita5.twittnuker.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import de.vanita5.twittnuker.constant.IntentConstants;

@ParcelablePlease
@JsonObject
public class HomeTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "hide_retweets")
    boolean hideRetweets;
    @JsonField(name = "hide_quotes")
    boolean hideQuotes;
    @JsonField(name = "hide_replies")
    boolean hideReplies;

    public boolean isHideRetweets() {
        return hideRetweets;
    }

    public void setHideRetweets(boolean hideRetweets) {
        this.hideRetweets = hideRetweets;
    }

    public boolean isHideQuotes() {
        return hideQuotes;
    }

    public void setHideQuotes(boolean hideQuotes) {
        this.hideQuotes = hideQuotes;
    }

    public boolean isHideReplies() {
        return hideReplies;
    }

    public void setHideReplies(boolean hideReplies) {
        this.hideReplies = hideReplies;
    }

    @Override
    public void copyToBundle(Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_RETWEETS, hideRetweets);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_QUOTES, hideQuotes);
        bundle.putBoolean(IntentConstants.EXTRA_HIDE_REPLIES, hideReplies);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HomeTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "HomeTabExtras{" +
                "hideRetweets=" + hideRetweets +
                ", hideQuotes=" + hideQuotes +
                ", hideReplies=" + hideReplies +
                "} " + super.toString();
    }

    public static final Creator<HomeTabExtras> CREATOR = new Creator<HomeTabExtras>() {
        public HomeTabExtras createFromParcel(Parcel source) {
            HomeTabExtras target = new HomeTabExtras();
            HomeTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public HomeTabExtras[] newArray(int size) {
            return new HomeTabExtras[size];
        }
    };
}