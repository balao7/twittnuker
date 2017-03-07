/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task

import android.app.Activity
import android.media.MediaScannerConnection
import android.widget.Toast

import de.vanita5.twittnuker.R
import java.io.File

class SaveMediaToGalleryTask(
        activity: Activity,
        fileInfo: FileInfo,
        destination: File
) : ProgressSaveFileTask(activity, destination, fileInfo) {

    override fun onFileSaved(savedFile: File, mimeType: String?) {
        val context = context ?: return
        MediaScannerConnection.scanFile(context, arrayOf(savedFile.path),
                arrayOf(mimeType), null)
        Toast.makeText(context, R.string.message_toast_saved_to_gallery, Toast.LENGTH_SHORT).show()
    }

    override fun onFileSaveFailed() {
        val context = context ?: return
        Toast.makeText(context, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
    }

}