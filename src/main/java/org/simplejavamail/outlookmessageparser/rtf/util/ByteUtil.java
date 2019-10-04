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

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public final class ByteUtil {
	
	private ByteUtil() {
	}
	
	@NotNull
	public static String hexToString(@NotNull final String hex, @NotNull final Charset charset) {
		return new String(hexStringToByteArray(hex), charset);
	}
	
	@NotNull
	/* from https://stackoverflow.com/a/140861/441662
		- Safe with leading zeros (unlike BigInteger) and with negative byte values (unlike Byte.parseByte)
		- Doesn't convert the String into a char[], or create StringBuilder and String objects for every single byte.
		- No library dependencies that may not be available
	 */
	public static byte[] hexStringToByteArray(@NotNull String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}