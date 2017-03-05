package org.simplejavamail.outlookmessageparser.model;

import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.poifs.filesystem.DocumentEntry;

/**
 * Convenience class for storing type information about a {@link DocumentEntry}.
 */
public class OutlookFieldInformation {

	/**
	 * The default value for both the {@link #clazz} and the {@link #type} properties.
	 */
	private static final String UNKNOWN = "unknown";

	/**
	 * The default value for the {@link #mapiType}
	 */
	public static final int UNKNOWN_MAPITYPE = -1;

	/**
	 * The class of the {@link DocumentEntry}.
	 */
	private final String clazz;

	/**
	 * The type of the {@link DocumentEntry}.
	 */
	private final String type;

	/**
	 * The mapi type of the {@link DocumentEntry}.
	 */
	private final int mapiType;

	/**
	 * Delegates to {@link #OutlookFieldInformation(String, String, int)} with values {@value #UNKNOWN}, {@value #UNKNOWN} and {@value #UNKNOWN_MAPITYPE}.
	 */
	public OutlookFieldInformation() {
		this(UNKNOWN, UNKNOWN, UNKNOWN_MAPITYPE);
	}

	/**
	 * Delegates to {@link #OutlookFieldInformation(String, String, int)} with given class, type and value {@value #UNKNOWN_MAPITYPE}.
	 *
	 * @deprecated
	 */
	@Deprecated
	public OutlookFieldInformation(String clazz, String type) {
		this(clazz, type, UNKNOWN_MAPITYPE);
	}

	/**
	 * Delegates to {@link #OutlookFieldInformation(String, String, int)} with given class, {@value #UNKNOWN} and given mapiType.
	 */
	public OutlookFieldInformation(String clazz, int mapiType) {
		this(clazz, UNKNOWN, mapiType);
	}

	/**
	 * @param clazz    The class of the {@link DocumentEntry}.
	 * @param type     The type of the {@link DocumentEntry} (see {@link MAPIProperty}).
	 * @param mapiType The mapiType of the {@link DocumentEntry} (see {@link MAPIProperty}).
	 */
	private OutlookFieldInformation(String clazz, String type, int mapiType) {
		this.clazz = clazz;
		this.type = type;
		this.mapiType = mapiType;
	}

	/**
	 * Bean getter for {@link #clazz}.
	 */
	public String getClazz() {
		return clazz;
	}

	/**
	 * Bean getter for {@link #type}.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Bean getter for {@link #mapiType}.
	 */
	public int getMapiType() {
		return mapiType;
	}
}