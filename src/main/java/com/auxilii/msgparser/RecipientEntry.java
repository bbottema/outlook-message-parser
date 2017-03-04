/*
 * msgparser - http://auxilii.com/msgparser
 * Copyright (C) 2007  Roman Kurmanowytsch
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.auxilii.msgparser;

import org.apache.poi.hsmf.datatypes.MAPIProperty;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a recipient's entry of the parsed .msg file. It provides informations like the 
 * email address and the display name.
 * @author thomas.misar
 * @author roman.kurmanowytsch
 */
public class RecipientEntry {
	protected static final Logger logger = Logger.getLogger(RecipientEntry.class.getName());	
	/**
	 * The address part of To: mail address.  
	 */
	protected String toEmail = null; 
	/**
	 * The address part of To: name.  
	 */
	protected String toName = null; 
	/**
	 * Contains all properties that are not
	 * covered by the special properties.
	 */
	protected final Map<Integer,Object> properties = new TreeMap<>();

	/**
	 * Sets the name/value pair in the {@link #properties}
	 * map. Some properties are put into
	 * special attributes (e.g., {@link #toEmail} when
	 * the property name is '0076'). 
	 * 
	 * @param msgProp The property to be set.
	 * @throws ClassCastException Thrown if the detected data
	 *  type does not match the expected data type.
	 */
	public void setProperty(MessageProperty msgProp) throws ClassCastException {
		String name = msgProp.getClazz();
		Object value = msgProp.getData();
		
		if ((name == null) || (value == null)) {
			return;
		}
		name = name.intern();
		
		int mapiClass = -1;
		try {
			mapiClass = Integer.parseInt(name, 16);
		} catch (NumberFormatException e) {
			logger.log(Level.FINEST, "Unexpected mapi class: "+name);  
		}
		
		switch(mapiClass) {
			case 0x3003: //EMAIL ADDRESS
			case 0x39fe:
				this.setToEmail((String) value);
				break;
			case 0x3001: //DISPLAY NAME
				this.setToName((String) value);
				break;
		}
		
		// save all properties (incl. those identified above)
		this.properties.put(mapiClass, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RecipientEntry that = (RecipientEntry) o;
		return Objects.equals(toEmail, that.toEmail) &&
				Objects.equals(toName, that.toName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(toEmail, toName);
	}

	/**
	 * @return the to: email
	 */
	public String getToEmail() {
		return toEmail;
	}

	/**
	 * @param toEmail the to email to be set
	 */
	public void setToEmail(String toEmail) {
		if(this.toEmail == null && toEmail != null && toEmail.contains("@")) {
			this.toEmail = toEmail;
		}
	}

	/**
	 * @return the to name
	 */
	public String getToName() {
		return toName;
	}

	/**
	 * @param toName the to name to be set
	 */
	public void setToName(String toName) {
		if(toName != null) {
			this.toName = toName;
		}
	}

	/**
	 * Provides a short representation of this recipient object <br>
	 * (e.g. 'Firstname Lastname &lt;firstname.lastname@domain.tld&gt;').
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.toName);
		if (sb.length() > 0) {
			sb.append(" ");
		}
		if ((this.toEmail != null) && (this.toEmail.length() > 0)) {
			sb.append("<").append(this.toEmail).append(">");
		}
		return sb.toString();
	}

	/**
	 * This method should no longer be used due to the fact that
	 * message properties are now stored with their keys being represented 
	 * as integers.
	 * @return All available keys properties have been found for.
	 */
	@Deprecated
	public Set<String> getProperties() {
		return getPropertiesAsHex();
	}
	
	/**
	 * This method provides a convenient way of retrieving
	 * property keys for all guys that like to stick to hex values.
	 * <br>Note that this method includes parsing of string values
	 * to integers which will be less efficient than using 
	 * {@link #getPropertyCodes()}.
	 * @return All available keys properties have been found for.
	 */
	public Set<String> getPropertiesAsHex() {
		Set<Integer> keySet = this.properties.keySet();
		Set<String> result = new HashSet<>();
		for(Integer k : keySet) {
			String s = String.format("%04x", k);
			result.add(s);
		}
		
		return result;
	}
	
	/**
	 * This method should no longer be used due to the fact that
	 * message properties are now stored with their keys being represented 
	 * as integers.
	 * <br>
	 * <br>
	 * Please refer to {@link #getPropertyCodes()} for dealing with 
	 * integer based keys.
	 * @return The value for the requested property.
	 */
	@Deprecated
	public Object getProperty(String name) {
		return getPropertyFromHex(name);
	}
	
	/**
	 * This method provides a convenient way of retrieving
	 * properties for all guys that like to stick to hex values.
	 * <br>Note that this method includes parsing of string values
	 * to integers which will be less efficient than using 
	 * {@link #getPropertyValue(Integer)}.
	 * @param name The hex notation of the property to be retrieved.
	 * @return The value for the requested property.
	 */
	public Object getPropertyFromHex(String name) {
		Integer i = -1 ;
		try {
			i = Integer.parseInt(name, 16);
		} catch (NumberFormatException e) {
			logger.log(Level.FINEST, "Could not parse integer: " + name);
		}
		return getPropertyValue(i);
	}
	
	/**
	 * This method returns a list of all available properties.
	 * @return All available keys properties have been found for.
	 */
	public Set<Integer> getPropertyCodes() {
		return this.properties.keySet();
	}
	
	/**
	 * This method retrieves the value for a specific property.
	 * <p>
	 * <b>NOTE:</b> You can also use fields defined within
	 * {@link MAPIProperty} to easily read certain properties.
	 * @param code The key for the property to be retrieved.
	 * @return The value of the specified property.
	 */
	public Object getPropertyValue(Integer code) {
		return this.properties.get(code);
	}
}
