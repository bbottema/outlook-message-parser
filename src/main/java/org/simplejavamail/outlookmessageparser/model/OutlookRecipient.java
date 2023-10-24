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

	// while parsing new properties, in some use cases the only emailaddress we get is actually encoded as name in the email
	// but if not, we should know when to replace it with the actually encoded address, where this flag helps us
	private boolean nameWasUsedAsAddress = false;

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

		if (mapiClass == 0x3003 || mapiClass == 0x39fe || mapiClass == 0x3001) {
			handleNameAddressProperty(mapiClass, (String) value);
		}

		// save all properties (incl. those identified above)
		properties.put(mapiClass, value);
	}

	private void handleNameAddressProperty(int mapiClass, String probablyNamePossiblyAddress) {
		if (mapiClass == 0x3003 || mapiClass == 0x39fe) { // address
			if ((this.address == null || nameWasUsedAsAddress) && probablyNamePossiblyAddress.contains("@")) {
				setAddress(probablyNamePossiblyAddress);
				nameWasUsedAsAddress = false;
			}
		} else if (mapiClass == 0x3001) { // name
			setName(probablyNamePossiblyAddress);
			// If no name+email was given, Outlook will encode the value as name, even if it actually is an addres
			// so just in that case, do a quick check to catch most use-cases where the name is actually the email address
			if (this.address == null && probablyNamePossiblyAddress.contains("@")) {
				setAddress(probablyNamePossiblyAddress);
				nameWasUsedAsAddress = true;
			} else if (this.address == null && probablyNamePossiblyAddress.startsWith("/o=ExchangeLabs/ou=Exchange Administrative Group")) {
				setAddress(probablyNamePossiblyAddress);
				nameWasUsedAsAddress = false;
			}
		}
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
		this.address = address;
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