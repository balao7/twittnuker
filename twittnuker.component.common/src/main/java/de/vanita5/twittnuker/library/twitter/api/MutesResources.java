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

package de.vanita5.twittnuker.library.twitter.api;

import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.IDs;
import de.vanita5.twittnuker.library.twitter.model.PageableResponseList;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.library.twitter.template.UserAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;

@Params(template = UserAnnotationTemplate.class)
public interface MutesResources {

    @POST("/mutes/users/create.json")
    User createMute(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/mutes/users/create.json")
    User createMuteByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;

    @POST("/mutes/users/destroy.json")
    User destroyMute(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/mutes/users/destroy.json")
    User destroyMuteByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;

    @GET("/mutes/users/ids.json")
    IDs getMutesUsersIDs(Paging paging) throws MicroBlogException;

    @GET("/mutes/users/list.json")
    PageableResponseList<User> getMutesUsersList(@Query Paging paging) throws MicroBlogException;
}