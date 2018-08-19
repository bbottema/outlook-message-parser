package org.simplejavamail.outlookmessageparser.rtf.util;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;

public final class ByteUtil {
	
	private ByteUtil() {
	}
	
	@Nonnull
	public static String hexToString(@Nonnull final String hex, @Nonnull final Charset charset) {
		return new String(hexStringToByteArray(hex), charset);
	}
	
	@Nonnull
	/* from https://stackoverflow.com/a/140861/441662
		- Safe with leading zeros (unlike BigInteger) and with negative byte values (unlike Byte.parseByte)
		- Doesn't convert the String into a char[], or create StringBuilder and String objects for every single byte.
		- No library dependencies that may not be available
	 */
	public static byte[] hexStringToByteArray(@Nonnull String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}