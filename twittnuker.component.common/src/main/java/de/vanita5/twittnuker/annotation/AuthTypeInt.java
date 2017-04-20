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

package de.vanita5.twittnuker.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AuthTypeInt.OAUTH, AuthTypeInt.XAUTH, AuthTypeInt.BASIC, AuthTypeInt.TWIP_O_MODE,
        AuthTypeInt.OAUTH2})
@Retention(RetentionPolicy.SOURCE)
public @interface AuthTypeInt {

    int OAUTH = 0;
    int XAUTH = 1;
    int BASIC = 2;
    int TWIP_O_MODE = 3;
    int OAUTH2 = 4;
}
