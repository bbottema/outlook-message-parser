package org.simplejavamail.outlookmessageparser.model;

import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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

		if ((name == null) || (value == null)) {
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
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
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
		if ((address != null) && (address.length() > 0)) {
			sb.append("<").append(address).append(">");
		}
		return sb.toString();
	}

	/**
	 * This method should no longer be used due to the fact that message properties are now stored with their keys being represented as integers.
	 *
	 * @return All available keys properties have been found for.
	 */
	@Deprecated
	public Set<String> getProperties() {
		return getPropertiesAsHex();
	}

	/**
	 * This method provides a convenient way of retrieving property keys for all guys that like to stick to hex values.
	 * <p>
	 * Note that this method includes parsing of string values to integers which will be less efficient than using {@link #getPropertyCodes()}.
	 *
	 * @return All available keys properties have been found for.
	 */
	private Set<String> getPropertiesAsHex() {
		final Set<Integer> keySet = properties.keySet();
		final Set<String> result = new HashSet<>();
		for (final Integer k : keySet) {
			final String s = String.format("%04x", k);
			result.add(s);
		}

		return result;
	}

	/**
	 * This method should no longer be used due to the fact that message properties are now stored with their keys being represented as integers.
	 * <p>
	 * Please refer to {@link #getPropertyCodes()} for dealing with integer based keys.
	 *
	 * @return The value for the requested property.
	 */
	@Deprecated
	public Object getProperty(final String name) {
		return getPropertyFromHex(name);
	}

	/**
	 * This method provides a convenient way of retrieving properties for all guys that like to stick to hex values.
	 * <p>
	 * Note that this method includes parsing of string values to integers which will be less efficient than using {@link #getPropertyValue(Integer)}.
	 *
	 * @return The value for the requested property for the given name.
	 */
	private Object getPropertyFromHex(final String name) {
		Integer i = -1;
		try {
			i = parseInt(name, 16);
		} catch (final NumberFormatException e) {
			LOGGER.error("Could not parse integer {}", name, e);
		}
		return getPropertyValue(i);
	}

	/**
	 * @return All available keys for properties found.
	 */
	public Set<Integer> getPropertyCodes() {
		return properties.keySet();
	}

	/**
	 * <b>NOTE:</b> You can also use fields defined within {@link MAPIProperty} to easily read certain properties.
	 *
	 * @return The property value of the specified code.
	 */
	private Object getPropertyValue(final Integer code) {
		return properties.get(code);
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