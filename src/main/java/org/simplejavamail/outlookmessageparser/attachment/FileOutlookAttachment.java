package org.simplejavamail.outlookmessageparser.attachment;

import org.simplejavamail.outlookmessageparser.OutlookMessageProperty;

/**
 * Implementation of the {@link OutlookAttachment} interface that represents a file attachment. It contains some useful information (as long as it is available
 * in the .msg file) like the attachment name, its size, etc.
 */
public class FileOutlookAttachment implements OutlookAttachment {

	/**
	 * The (by Outlook) shortened filename of the attachment.
	 */
	private String filename = null;
	/**
	 * The full filename of the attachment.
	 */
	private String longFilename = null;
	/**
	 * Mime type of the attachment
	 */
	private String mimeTag = null;
	/**
	 * The extension of the attachment (may not be set).
	 */
	private String extension = null;
	/**
	 * The attachment itself as a byte array.
	 */
	private byte[] data = null;
	/**
	 * The size of the attachment.
	 */
	private long size = -1;

	/**
	 * Sets the property specified by the name parameter. Unknown names are ignored.
	 */
	public void setProperty(OutlookMessageProperty msgProp) {
		String name = msgProp.getClazz();
		Object value = msgProp.getData();

		if (name != null && value != null) {
			switch (name) {
				case "3701":
					this.setSize(msgProp.getSize());
					this.setData((byte[]) value);
					break;
				case "3704":
					this.setFilename((String) value);
					break;
				case "3707":
					this.setLongFilename((String) value);
					break;
				case "370e":
					this.setMimeTag((String) value);
					break;
				case "3703":
					this.setExtension((String) value);
					break;
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
	public String getExtension() {
		return extension;
	}

	/**
	 * Bean setter for {@link #extension}.
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * Bean getter for {@link #filename}.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Bean setter for {@link #filename}.
	 */
	public void setFilename(String filename) {
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
	public void setLongFilename(String longFilename) {
		this.longFilename = longFilename;
	}

	/**
	 * Bean getter for {@link #mimeTag}.
	 */
	public String getMimeTag() {
		return mimeTag;
	}

	/**
	 * Bean setter for {@link #mimeTag}.
	 */
	public void setMimeTag(String mimeTag) {
		this.mimeTag = mimeTag;
	}

	/**
	 * Bean getter for {@link #data}.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Bean setter for {@link #data}.
	 */
	public void setData(byte[] data) {
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
	public void setSize(long size) {
		this.size = size;
	}
}
