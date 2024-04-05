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

	private static final String X500_ADDRESS_PATTERN = "/o=[^/]+/ou=[^/]+(?:/cn=[^/]+)*";

	/**
	 * Contains all properties that are not covered by the special properties.
	 */
	private final Map<Integer, Object> properties = new TreeMap<>();

	private String name;
	private String address;
	private String x500Address;

	// while parsing new properties, in some use cases the only emailaddress we get is actually encoded as name in the email
	// but if not, we should know when to replace it with the actually encoded address, where this flag helps us
	private boolean nameWasUsedAsAddress;

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

	private void handleNameAddressProperty(final int mapiClass, final String probablyNamePossiblyAddress) {
		if (mapiClass == 0x3001) { // name
			handleNameProperty(probablyNamePossiblyAddress);
		} else if (mapiClass == 0x3003 || mapiClass == 0x39fe) { // address
			handleAddressProperty(probablyNamePossiblyAddress);
		}
	}

	private void handleNameProperty(final String probablyNamePossiblyAddress) {
		setName(probablyNamePossiblyAddress);
		// If no name+email was given, Outlook will encode the value as name, even if it actually is an addres
		// so just in that case, do a quick check to catch most use-cases where the name is actually the email address
		if (address == null && probablyNamePossiblyAddress.contains("@")) {
			setAddress(probablyNamePossiblyAddress);
			nameWasUsedAsAddress = true;
		}
	}

	private void handleAddressProperty(final String probablyNamePossiblyAddress) {
		if (probablyNamePossiblyAddress.contains("@") && (address == null || nameWasUsedAsAddress || address.matches(X500_ADDRESS_PATTERN))) {
			setAddress(probablyNamePossiblyAddress);
			nameWasUsedAsAddress = false;
		} else if (probablyNamePossiblyAddress.matches(X500_ADDRESS_PATTERN)) {
			if (address == null) {
				setAddress(probablyNamePossiblyAddress);
			}
			setX500Address(probablyNamePossiblyAddress);
			nameWasUsedAsAddress = false;
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
				Objects.equals(x500Address, that.x500Address) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, x500Address, name);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (sb.length() > 0) {
			sb.append(" ");
		}
		if (address != null && !address.isEmpty()) {
			sb.append("<").append(address).append(">");
		}
		if (x500Address != null && !x500Address.isEmpty()) {
			sb.append("<").append(x500Address).append(">");
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
	 * Bean getter for {@link #x500Address}.
	 */
	public String getX500Address() {
		return x500Address;
	}

	/**
	 * Bean setter for {@link #address}.
	 */
	public void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * Bean setter for {@link #address}.
	 */
	public void setX500Address(final String x500Address) {
		this.x500Address = x500Address;
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
