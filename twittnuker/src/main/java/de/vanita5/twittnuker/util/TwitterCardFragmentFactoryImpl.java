/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import de.vanita5.twittnuker.model.ParcelableCardEntity;

public final class TwitterCardFragmentFactoryImpl extends TwitterCardFragmentFactory {

    private static final String YOUTUBE_DATA_API_KEY = "AIzaSyCVdCIMFFxdNqHnCPrJ9yKUzoTfs8jhYGc";

    @Override
    public Fragment createAnimatedGifFragment(ParcelableCardEntity card) {
        return null;
    }

    @Override
    public Fragment createAudioFragment(ParcelableCardEntity card) {
        return null;
    }

    @Override
    public Fragment createPlayerFragment(ParcelableCardEntity card) {
        if (Boolean.parseBoolean("true")) return null;
        final String appUrlResolved = card.getString("app_url_resolved");
        final String domain = card.getString("domain");
        if (domain != null && appUrlResolved != null) {
            final Uri uri = Uri.parse(appUrlResolved);
            final String paramV = uri.getQueryParameter("v");
            if ("www.youtube.com".equals(domain) && paramV != null) {
                final YouTubePlayerSupportFragment fragment = YouTubePlayerSupportFragment.newInstance();
                fragment.initialize(YOUTUBE_DATA_API_KEY, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                        if (!wasRestored) {
                            player.cueVideo(paramV);
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
                        final FragmentActivity activity = fragment.getActivity();
                        if (activity == null) return;
//                        if (errorReason.isUserRecoverableError()) {
//                            errorReason.getErrorDialog(activity, RECOVERY_DIALOG_REQUEST).show();
//                        } else {
//                            Toast.makeText(activity, errorReason.toString(), Toast.LENGTH_LONG).show();
//                        }
                    }
                });
                return fragment;
            }
        }
        return null;
    }

}