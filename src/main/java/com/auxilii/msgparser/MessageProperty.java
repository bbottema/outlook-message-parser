// ============================================================================
// Braintribe IT Technologies GmbH
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2012
// www.braintribe.com
// ============================================================================
// last changed date: $LastChangedDate: $
// last changed by: $LastChangedBy: $
// ============================================================================
package com.auxilii.msgparser;

/**
 * Represents a message property holding the type of data and the data itself.
 * @author thomas.misar
 *
 */
public class MessageProperty {
	
	private final String clazz;
	private final Object data;
	private final int size;


	public MessageProperty(String clazz, Object data, int size) {
		super();
		this.clazz = clazz;
		this.data = data;
		this.size = size;
	}

	/**
	 * A 4 digit code representing the property type.
	 * @return A string representation of the property type.
	 */
	public String getClazz() {
		return clazz;
	}

	/**
	 * An object holding the property data.
	 * @return The property data.
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * The size of the data.
	 * @return The number of bytes of the data object.
	 */
	public int getSize() {
		return size;
	}
	
}
