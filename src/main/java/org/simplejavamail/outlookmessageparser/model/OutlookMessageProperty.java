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
		this.data = data;
		this.size = size;
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