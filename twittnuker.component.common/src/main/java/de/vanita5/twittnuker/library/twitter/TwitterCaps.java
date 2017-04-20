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

package de.vanita5.twittnuker.library.twitter;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;

import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.CardDataMap;
import de.vanita5.twittnuker.library.twitter.model.CardResponse;
import de.vanita5.twittnuker.library.twitter.model.CreateCardData;
import de.vanita5.twittnuker.library.twitter.model.CreateCardResult;

public interface TwitterCaps {

    @GET("/v2/capi/passthrough/1")
    CardResponse getPassThrough(@Query CardDataMap params)
            throws MicroBlogException;

    @POST("/v2/capi/passthrough/1")
    @BodyType(BodyType.FORM)
    CardResponse sendPassThrough(@Param CardDataMap params) throws MicroBlogException;

    @POST("/v2/cards/create.json")
    CreateCardResult createCard(@Param("card_data") CreateCardData cardData) throws MicroBlogException;
}