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

package de.vanita5.microblog.library.mastodon.model;

import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 2017/3/27.
 */

public class TimelineOption extends SimpleValueMap {
    public TimelineOption excludeReplies(boolean excludeReplies) {
        if (excludeReplies) {
            put("exclude_replies", true);
        } else {
            remove("exclude_replies");
        }
        return this;
    }

    public TimelineOption onlyMedia(boolean onlyMedia) {
        put("only_media", onlyMedia);
        return this;
    }
}