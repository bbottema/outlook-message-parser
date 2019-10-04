/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simplejavamail.outlookmessageparser.model;

import javax.activation.MimetypesFileTypeMap;

/**
 * Implementation of the {@link OutlookAttachment} interface that represents a file attachment. It contains some useful information (as long as it is available
 * in the .msg file) like the attachment name, its size, etc.
 */
public class OutlookFileAttachment implements OutlookAttachment {

	/**
	 * The (by Outlook) shortened filename of the attachment.
	 */
	private String filename;
	/**
	 * The full filename of the attachment.
	 */
	private String longFilename;
	/**
	 * Mime type of the attachment
	 */
	private String mimeTag;
	/**
	 * The extension of the attachment (may not be set).
	 */
	private String extension;
	/**
	 * The attachment itself as a byte array.
	 */
	private byte[] data;
	/**
	 * The size of the attachment.
	 */
	private long size = -1;

	/**
	 * Sets the property specified by the name parameter. Unknown names are ignored.
	 */
	public void setProperty(final OutlookMessageProperty msgProp) {
		final String name = msgProp.getClazz();
		final Object value = msgProp.getData();

		if (name != null && value != null) {
			switch (name) {
				case "3701":
					setSize(msgProp.getSize());
					setData((byte[]) value);
					break;
				case "3704":
					setFilename((String) value);
					break;
				case "3707":
					setLongFilename((String) value);
					break;
				case "370e":
					setMimeTag((String) value);
					break;
				case "3703":
					setExtension((String) value);
					break;
				default:
					// property to ignore, for full list see properties-list.txt
			}
		}
	}
	
	public void checkSmimeFilename() {
		if (this.filename == null && this.mimeTag != null) {
			if (this.mimeTag.contains("multipart/signed")) {
				if (!this.mimeTag.contains("protocol") || this.mimeTag.contains("protocol=\"application/pkcs7-signature\"")) {
					this.filename = "smime.p7s";
				}
			}
		}
	}
	
	public void checkMimeTag() {
		if (this.mimeTag == null || this.mimeTag.length() == 0) {
			if (this.filename != null && this.filename.length() > 0) {
				this.mimeTag = MimeType.getContentType(this.filename);
			} else if (this.longFilename != null && this.longFilename.length() > 0) {
				this.mimeTag = MimeType.getContentType(this.longFilename);
			}
		}
	}

	@Override
	public String toString() {
		return (longFilename != null) ? longFilename : filename;
	}

	/**
	 * Bean getter for {@link #extension}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getExtension() {
		return extension;
	}

	/**
	 * Bean setter for {@link #extension}.
	 */
	void setExtension(final String extension) {
		this.extension = extension;
	}

	/**
	 * Bean getter for {@link #filename}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getFilename() {
		return filename;
	}

	/**
	 * Bean setter for {@link #filename}.
	 */
	void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * Bean getter for {@link #longFilename}.
	 */
	public String getLongFilename() {
		return longFilename;
	}

	/**
	 * Bean setter for {@link #longFilename}.
	 */
	void setLongFilename(final String longFilename) {
		this.longFilename = longFilename;
	}

	/**
	 * Bean getter for {@link #mimeTag}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getMimeTag() {
		return mimeTag;
	}

	/**
	 * Bean setter for {@link #mimeTag}.
	 */
	void setMimeTag(final String mimeTag) {
		this.mimeTag = mimeTag;
	}

	/**
	 * Bean getter for {@link #data}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public byte[] getData() {
		return data.clone();
	}

	/**
	 * Bean setter for {@link #data}.
	 */
	void setData(final byte[] data) {
		this.data = data;
	}

	/**
	 * Bean getter for {@link #size}.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Bean setter for {@link #size}.
	 */
	void setSize(final long size) {
		this.size = size;
	}
}
