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

package de.vanita5.microblog.library.twitter.api;

import de.vanita5.microblog.library.MicroBlogException;
import de.vanita5.microblog.library.twitter.model.Paging;
import de.vanita5.microblog.library.twitter.model.ResponseList;
import de.vanita5.microblog.library.twitter.model.Status;
import de.vanita5.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;

@SuppressWarnings("RedundantThrows")
@Params(template = StatusAnnotationTemplate.class)
public interface FavoritesResources {

    @POST("/favorites/create.json")
    Status createFavorite(@Param("id") String id) throws MicroBlogException;

    @POST("/favorites/destroy.json")
    Status destroyFavorite(@Param("id") String id) throws MicroBlogException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites() throws MicroBlogException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("user_id") String userId, @Query Paging paging) throws MicroBlogException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavoritesByScreenName(@Query("screen_name") String screenName, @Query Paging paging) throws MicroBlogException;
}