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

package de.vanita5.twittnuker.model;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.model.message.MessageExtras;
import de.vanita5.twittnuker.model.message.StickerExtras;
import de.vanita5.twittnuker.model.util.MessageExtrasConverter;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages;

import java.util.Arrays;

@JsonObject
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessage {

    @CursorField(value = Messages._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @JsonField(name = "account_key")
    @CursorField(value = Messages.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @JsonField(name = "id")
    @CursorField(Messages.MESSAGE_ID)
    public String id;

    @JsonField(name = "conversation_id")
    @CursorField(Messages.CONVERSATION_ID)
    public String conversation_id;

    @JsonField(name = "type")
    @CursorField(Messages.MESSAGE_TYPE)
    @Type
    public String message_type;

    @JsonField(name = "timestamp")
    @CursorField(Messages.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @JsonField(name = "local_timestamp")
    @CursorField(Messages.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @JsonField(name = "text_unescaped")
    @CursorField(Messages.TEXT_UNESCAPED)
    public String text_unescaped;

    @JsonField(name = "media")
    @CursorField(value = Messages.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;

    @JsonField(name = "spans")
    @CursorField(value = Messages.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;
    @CursorField(value = Messages.EXTRAS, converter = MessageExtrasConverter.class)
    public MessageExtras extras;

    @JsonField(name = "extras")
    @ParcelableNoThanks
    InternalExtras internalExtras;

    @JsonField(name = "sender_key")
    @CursorField(value = Messages.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;

    @JsonField(name = "recipient_key")
    @CursorField(value = Messages.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @JsonField(name = "is_outgoing")
    @CursorField(Messages.IS_OUTGOING)
    public boolean is_outgoing;

    @JsonField(name = "request_cursor")
    @CursorField(value = Messages.REQUEST_CURSOR)
    public String request_cursor;

    @Override
    public String toString() {
        return "ParcelableMessage{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", id='" + id + '\'' +
                ", conversation_id='" + conversation_id + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", is_outgoing=" + is_outgoing +
                ", request_cursor='" + request_cursor + '\'' +
                '}';
    }


    @OnPreJsonSerialize
    void beforeJsonSerialize() {
        internalExtras = InternalExtras.from(extras);
    }


    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (internalExtras != null) {
            extras = internalExtras.getExtras();
        }
    }


    @StringDef({Type.TEXT, Type.STICKER})
    public @interface Type {
        String TEXT = "text";
        String STICKER = "sticker";
    }

    @JsonObject
    static class InternalExtras {
        @JsonField(name = "sticker")
        StickerExtras sticker;

        public static InternalExtras from(final MessageExtras extras) {
            if (extras == null) return null;
            InternalExtras result = new InternalExtras();
            if (extras instanceof StickerExtras) {
                result.sticker = (StickerExtras) extras;
            } else {
                return null;
            }
            return result;
        }

        public MessageExtras getExtras() {
            if (sticker != null) {
                return sticker;
            }
            return null;
        }
    }
}