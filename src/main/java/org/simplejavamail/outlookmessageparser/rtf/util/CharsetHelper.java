package org.simplejavamail.outlookmessageparser.rtf.util;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class CharsetHelper {
	private static String[] CHARSET_PREFIXES = {"", "cp", "iso-", "ibm", "x-windows-", "ms"};
	
	public static final Charset WINDOWS_CHARSET = Charset.forName("CP1252");
	
	public static Charset findCharset(Integer rtfCodePage) {
		for (String prefix : CHARSET_PREFIXES) {
			try {
				return Charset.forName(prefix + rtfCodePage);
			} catch (UnsupportedCharsetException ignore) {
				// ignore
			}
		}
		throw new UnsupportedCharsetException("" + rtfCodePage);
	}
}