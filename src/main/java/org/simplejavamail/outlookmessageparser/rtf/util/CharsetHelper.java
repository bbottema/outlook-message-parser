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