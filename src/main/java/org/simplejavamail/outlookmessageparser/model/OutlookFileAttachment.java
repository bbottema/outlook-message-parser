package org.simplejavamail.outlookmessageparser.model;

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
					// don't do anything, currently I don't even know if this is a functionally legal state
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
	private void setExtension(final String extension) {
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
	private void setFilename(final String filename) {
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
	private void setLongFilename(final String longFilename) {
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
	private void setMimeTag(final String mimeTag) {
		this.mimeTag = mimeTag;
	}

	/**
	 * Bean getter for {@link #data}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public byte[] getData() {
		return data;
	}

	/**
	 * Bean setter for {@link #data}.
	 */
	private void setData(final byte[] data) {
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
	private void setSize(final long size) {
		this.size = size;
	}
}
