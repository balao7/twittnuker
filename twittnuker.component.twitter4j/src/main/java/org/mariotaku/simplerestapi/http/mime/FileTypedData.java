/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.simplerestapi.http.mime;

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mariotaku on 15/5/6.
 */
public class FileTypedData implements TypedData {

	private final File file;
	private final ContentType contentType;
	private FileInputStream stream;

	public FileTypedData(File file, ContentType contentType) {
		this.file = file;
		this.contentType = contentType;
	}

	public FileTypedData(File file) {
		this(file, null);
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public void writeTo(OutputStream os) throws IOException {
		Utils.copyStream(stream(), os);
	}

	@Override
	public InputStream stream() throws IOException {
		if (stream != null) return stream;
		return stream = new FileInputStream(file);
	}

	@Override
	public void close() throws IOException {
		if (stream != null) {
			stream.close();
		}
	}

	@Override
	public ContentType contentType() {
		if (contentType == null) {
			return ContentType.OCTET_STREAM;
		}
		return contentType;
	}

	@Override
	public String contentEncoding() {
		return null;
	}
}