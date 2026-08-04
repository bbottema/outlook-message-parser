package org.simplejavamail.outlookmessageparser.model;

/**
 * Represents a message property holding the type of data and the data itself.
 */
public class OutlookMessageProperty {

	/**
	 * A 4 digit code representing the property type.
	 */
	private final String clazz;
	private final Object data;
	private final int size;

	public OutlookMessageProperty(final String clazz, final Object data, final int size) {
		this.clazz = clazz;
		this.data = removeTrailingNulCharacters(data);
		this.size = size;
	}

	private static Object removeTrailingNulCharacters(final Object data) {
		if (!(data instanceof String)) {
			return data;
		}

		final String stringData = (String) data;
		int endIndex = stringData.length();
		while (endIndex > 0 && stringData.charAt(endIndex - 1) == '\0') {
			endIndex--;
		}
		return endIndex == stringData.length() ? stringData : stringData.substring(0, endIndex);
	}

	/**
	 * Bean getter for {@link #clazz}.
	 */
	public String getClazz() {
		return clazz;
	}

	public Object getData() {
		return data;
	}

	public int getSize() {
		return size;
	}
}
