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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.Integer.parseInt;

/**
 * This class represents a recipient's entry of the parsed .msg file. It provides informations like the  email address and the display name.
 */
public class OutlookRecipient {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutlookRecipient.class);

	/**
	 * Contains all properties that are not covered by the special properties.
	 */
	private final Map<Integer, Object> properties = new TreeMap<>();

	private String name;
	private String address;

	/**
	 * Sets the name/value pair in the {@link #properties} map. Some properties are put into special attributes (e.g., {@link #address} when the property name
	 * is '0076').
	 *
	 * @param msgProp The property to be set.
	 */
	public void setProperty(final OutlookMessageProperty msgProp) {
		String name = msgProp.getClazz();
		final Object value = msgProp.getData();

		if (name == null || value == null) {
			return;
		}
		name = name.intern();

		int mapiClass = -1;
		try {
			mapiClass = parseInt(name, 16);
		} catch (final NumberFormatException e) {
			LOGGER.error("Unexpected mapi class: {}", name, e);
		}

		if (mapiClass == 0x3003 || mapiClass == 0x39fe) {
			setAddress((String) value);
		} else if (mapiClass == 0x3001) {
			setName((String) value);
		}

		// save all properties (incl. those identified above)
		properties.put(mapiClass, value);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final OutlookRecipient that = (OutlookRecipient) o;
		return Objects.equals(address, that.address) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, name);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (sb.length() > 0) {
			sb.append(" ");
		}
		if (address != null && address.length() > 0) {
			sb.append("<").append(address).append(">");
		}
		return sb.toString();
	}

	/**
	 * @return All available keys for properties found.
	 */
	public Set<Integer> getPropertyCodes() {
		return properties.keySet();
	}

	/**
	 * Bean getter for {@link #address}.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Bean setter for {@link #address}.
	 */
	public void setAddress(final String address) {
		if (this.address == null && address != null && address.contains("@")) {
			this.address = address;
		}
	}

	/**
	 * Bean getter for {@link #name}.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Bean setter for {@link #name}.
	 */
	public void setName(final String name) {
		if (name != null) {
			this.name = name;
		}
	}
}