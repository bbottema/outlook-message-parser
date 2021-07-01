/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.simplejavamail.jakarta.mail.internet;

import java.util.*;

import org.simplejavamail.com.sun.mail.util.BEncoderStream;
import org.simplejavamail.com.sun.mail.util.LineInputStream;
import org.simplejavamail.com.sun.mail.util.PropUtil;
import org.simplejavamail.com.sun.mail.util.QEncoderStream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * This is a utility class that provides various MIME related
 * functionality. <p>
 *
 * There are a set of methods to encode and decode MIME headers as 
 * per RFC 2047.  Note that, in general, these methods are
 * <strong>not</strong> needed when using methods such as
 * <code>setSubject</code> and <code>setRecipients</code>; Jakarta Mail
 * will automatically encode and decode data when using these "higher
 * level" methods.  The methods below are only needed when maniuplating
 * raw MIME headers using <code>setHeader</code> and <code>getHeader</code>
 * methods.  A brief description on handling such headers is given below: <p>
 *
 * RFC 822 mail headers <strong>must</strong> contain only US-ASCII
 * characters. Headers that contain non US-ASCII characters must be
 * encoded so that they contain only US-ASCII characters. Basically,
 * this process involves using either BASE64 or QP to encode certain
 * characters. RFC 2047 describes this in detail. <p>
 *
 * In Java, Strings contain (16 bit) Unicode characters. ASCII is a
 * subset of Unicode (and occupies the range 0 - 127). A String
 * that contains only ASCII characters is already mail-safe. If the
 * String contains non US-ASCII characters, it must be encoded. An
 * additional complexity in this step is that since Unicode is not
 * yet a widely used charset, one might want to first charset-encode
 * the String into another charset and then do the transfer-encoding.
 * <p>
 * Note that to get the actual bytes of a mail-safe String (say,
 * for sending over SMTP), one must do 
 * <blockquote><pre>
 *
 *	byte[] bytes = string.getBytes("iso-8859-1");	
 *
 * </pre></blockquote><p>
 * 
 * The <code>setHeader</code> and <code>addHeader</code> methods
 * on MimeMessage and MimeBodyPart assume that the given header values
 * are Unicode strings that contain only US-ASCII characters. Hence
 * the callers of those methods must insure that the values they pass
 * do not contain non US-ASCII characters. The methods in this class 
 * help do this. <p>
 *
 * The <code>getHeader</code> family of methods on MimeMessage and
 * MimeBodyPart return the raw header value. These might be encoded
 * as per RFC 2047, and if so, must be decoded into Unicode Strings.
 * The methods in this class help to do this. <p>
 *
 * Several System properties control strict conformance to the MIME
 * spec.  Note that these are not session properties but must be set
 * globally as System properties. <p>
 *
 * The <code>mail.mime.decodetext.strict</code> property controls
 * decoding of MIME encoded words.  The MIME spec requires that encoded
 * words start at the beginning of a whitespace separated word.  Some
 * mailers incorrectly include encoded words in the middle of a word.
 * If the <code>mail.mime.decodetext.strict</code> System property is
 * set to <code>"false"</code>, an attempt will be made to decode these
 * illegal encoded words. The default is true. <p>
 *
 * The <code>mail.mime.encodeeol.strict</code> property controls the
 * choice of Content-Transfer-Encoding for MIME parts that are not of
 * type "text".  Often such parts will contain textual data for which
 * an encoding that allows normal end of line conventions is appropriate.
 * In rare cases, such a part will appear to contain entirely textual
 * data, but will require an encoding that preserves CR and LF characters
 * without change.  If the <code>mail.mime.encodeeol.strict</code>
 * System property is set to <code>"true"</code>, such an encoding will
 * be used when necessary.  The default is false. <p>
 *
 * In addition, the <code>mail.mime.charset</code> System property can
 * be used to specify the default MIME charset to use for encoded words
 * and text parts that don't otherwise specify a charset.  Normally, the
 * default MIME charset is derived from the default Java charset, as
 * specified in the <code>file.encoding</code> System property.  Most
 * applications will have no need to explicitly set the default MIME
 * charset.  In cases where the default MIME charset to be used for
 * mail messages is different than the charset used for files stored on
 * the system, this property should be set. <p>
 *
 * The current implementation also supports the following System property.
 * <p>
 * The <code>mail.mime.ignoreunknownencoding</code> property controls
 * whether unknown values in the <code>Content-Transfer-Encoding</code>
 * header, as passed to the <code>decode</code> method, cause an exception.
 * If set to <code>"true"</code>, unknown values are ignored and 8bit
 * encoding is assumed.  Otherwise, unknown values cause a MessagingException
 * to be thrown.
 *
 * @author  John Mani
 * @author  Bill Shannon
 */

public class MimeUtility {

    // This class cannot be instantiated
    private MimeUtility() { }

    public static final int ALL = -1;

    // cached map of whether a charset is compatible with ASCII
    // Map<String,Boolean>
    private static final Map<String, Boolean> nonAsciiCharsetMap
	    = new HashMap<>();

    private static final boolean decodeStrict =
	PropUtil.getBooleanSystemProperty("mail.mime.decodetext.strict", true);
    private static final boolean encodeEolStrict =
	PropUtil.getBooleanSystemProperty("mail.mime.encodeeol.strict", false);
    private static final boolean ignoreUnknownEncoding =
	PropUtil.getBooleanSystemProperty(
	    "mail.mime.ignoreunknownencoding", false);
    private static final boolean allowUtf8 =
	PropUtil.getBooleanSystemProperty("mail.mime.allowutf8", false);
    /*
     * The following two properties allow disabling the fold()
     * and unfold() methods and reverting to the previous behavior.
     * They should never need to be changed and are here only because
     * of my paranoid concern with compatibility.
     */
    private static final boolean foldEncodedWords =
	PropUtil.getBooleanSystemProperty("mail.mime.foldencodedwords", false);
    private static final boolean foldText =
	PropUtil.getBooleanSystemProperty("mail.mime.foldtext", true);


    /**
     * Encode a RFC 822 "word" token into mail-safe form as per
     * RFC 2047. <p>
     *
     * The given Unicode string is examined for non US-ASCII
     * characters. If the string contains only US-ASCII characters,
     * it is returned as-is.  If the string contains non US-ASCII
     * characters, it is first character-encoded using the platform's
     * default charset, then transfer-encoded using either the B or 
     * Q encoding. The resulting bytes are then returned as a Unicode 
     * string containing only ASCII  characters. <p>
     * 
     * This method is meant to be used when creating RFC 822 "phrases".
     * The InternetAddress class, for example, uses this to encode
     * it's 'phrase' component.
     *
     * @param	word	Unicode string
     * @return	Array of Unicode strings containing only US-ASCII 
     *		characters.
     * @exception UnsupportedEncodingException if the encoding fails
     */
    public static String encodeWord(String word) 
			throws UnsupportedEncodingException {
	return encodeWord(word, null, null);
    }

    /**
     * Encode a RFC 822 "word" token into mail-safe form as per
     * RFC 2047. <p>
     *
     * The given Unicode string is examined for non US-ASCII
     * characters. If the string contains only US-ASCII characters,
     * it is returned as-is.  If the string contains non US-ASCII
     * characters, it is first character-encoded using the specified
     * charset, then transfer-encoded using either the B or Q encoding.
     * The resulting bytes are then returned as a Unicode string 
     * containing only ASCII characters. <p>
     * 
     * @param	word	Unicode string
     * @param	charset	the MIME charset
     * @param	encoding the encoding to be used. Currently supported
     *		values are "B" and "Q". If this parameter is null, then
     *		the "Q" encoding is used if most of characters to be
     *		encoded are in the ASCII charset, otherwise "B" encoding
     *		is used.
     * @return	Unicode string containing only US-ASCII characters
     * @exception UnsupportedEncodingException if the encoding fails
     */
    public static String encodeWord(String word, String charset, 
				    String encoding)
    			throws UnsupportedEncodingException {
	return encodeWord(word, charset, encoding, true);
    }

    /*
     * Encode the given string. The parameter 'encodingWord' should
     * be true if a RFC 822 "word" token is being encoded and false if a
     * RFC 822 "text" token is being encoded. This is because the 
     * "Q" encoding defined in RFC 2047 has more restrictions when
     * encoding "word" tokens. (Sigh)
     */ 
    private static String encodeWord(String string, String charset,
				     String encoding, boolean encodingWord)
			throws UnsupportedEncodingException {

	// If 'string' contains only US-ASCII characters, just
	// return it.
	int ascii = checkAscii(string);
	if (ascii == ALL_ASCII)
	    return string;

	// Else, apply the specified charset conversion.
	String jcharset;
	if (charset == null) { // use default charset
	    jcharset = getDefaultJavaCharset(); // the java charset
	    charset = getDefaultMIMECharset(); // the MIME equivalent
	} else // MIME charset -> java charset
	    jcharset = javaCharset(charset);

	// If no transfer-encoding is specified, figure one out.
	if (encoding == null) {
	    if (ascii != MOSTLY_NONASCII)
		encoding = "Q";
	    else
		encoding = "B";
	}

	boolean b64;
	if (encoding.equalsIgnoreCase("B")) 
	    b64 = true;
	else if (encoding.equalsIgnoreCase("Q"))
	    b64 = false;
	else
	    throw new UnsupportedEncodingException(
			"Unknown transfer encoding: " + encoding);

	StringBuilder outb = new StringBuilder(); // the output buffer
	doEncode(string, b64, jcharset, 
		 // As per RFC 2047, size of an encoded string should not
		 // exceed 75 bytes.
		 // 7 = size of "=?", '?', 'B'/'Q', '?', "?="
		 75 - 7 - charset.length(), // the available space
		 "=?" + charset + "?" + encoding + "?", // prefix
		 true, encodingWord, outb);

	return outb.toString();
    }

    private static void doEncode(String string, boolean b64, 
		String jcharset, int avail, String prefix, 
		boolean first, boolean encodingWord, StringBuilder buf)
			throws UnsupportedEncodingException {

	// First find out what the length of the encoded version of
	// 'string' would be.
	byte[] bytes = string.getBytes(jcharset);
	int len;
	if (b64) // "B" encoding
	    len = BEncoderStream.encodedLength(bytes);
	else // "Q"
	    len = QEncoderStream.encodedLength(bytes, encodingWord);
	
	int size;
	if ((len > avail) && ((size = string.length()) > 1)) { 
	    // If the length is greater than 'avail', split 'string'
	    // into two and recurse.
	    // Have to make sure not to split a Unicode surrogate pair.
	    int split = size / 2;
	    if (Character.isHighSurrogate(string.charAt(split-1)))
		split--;
	    if (split > 0)
		doEncode(string.substring(0, split), b64, jcharset, 
			 avail, prefix, first, encodingWord, buf);
	    doEncode(string.substring(split, size), b64, jcharset,
		     avail, prefix, false, encodingWord, buf);
	} else {
	    // length <= than 'avail'. Encode the given string
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    OutputStream eos; // the encoder
	    if (b64) // "B" encoding
		eos = new BEncoderStream(os);
	    else // "Q" encoding
		eos = new QEncoderStream(os, encodingWord);
	    
	    try { // do the encoding
		eos.write(bytes);
		eos.close();
	    } catch (IOException ioex) { }

	    byte[] encodedBytes = os.toByteArray(); // the encoded stuff
	    // Now write out the encoded (all ASCII) bytes into our
	    // StringBuilder
	    if (!first) // not the first line of this sequence
		if (foldEncodedWords)
		    buf.append("\r\n "); // start a continuation line
		else
		    buf.append(" "); // line will be folded later

	    buf.append(prefix);
	    for (int i = 0; i < encodedBytes.length; i++)
		buf.append((char)encodedBytes[i]);
	    buf.append("?="); // terminate the current sequence
	}
    }

    /**
	 * A utility method to quote a word, if the word contains any
	 * characters from the specified 'specials' list.<p>
	 *
	 * The <code>HeaderTokenizer</code> class defines two special
	 * sets of delimiters - MIME and RFC 822. <p>
	 *
	 * This method is typically used during the generation of 
	 * RFC 822 and MIME header fields.
	 *
	 * @param	word	word to be quoted
	 * @param	specials the set of special characters
	 * @return		the possibly quoted word
	 * @see	org.simplejavamail.jakarta.mail.internet.HeaderTokenizer#MIME
	 * @see	org.simplejavamail.jakarta.mail.internet.HeaderTokenizer#RFC822
	 */
	public static String quote(String word, String specials) {
	int len = word == null ? 0 : word.length();
	if (len == 0)
	    return "\"\"";	// an empty string is handled specially
	
	/*
	 * Look for any "bad" characters, Escape and
	 *  quote the entire string if necessary.
	 */
	boolean needQuoting = false;
	for (int i = 0; i < len; i++) {
	    char c = word.charAt(i);
	    if (c == '"' || c == '\\' || c == '\r' || c == '\n') {
		// need to escape them and then quote the whole string
		StringBuilder sb = new StringBuilder(len + 3);
		sb.append('"');
		sb.append(word.substring(0, i));
		int lastc = 0;
		for (int j = i; j < len; j++) {
		    char cc = word.charAt(j);
		    if ((cc == '"') || (cc == '\\') || 
			(cc == '\r') || (cc == '\n'))
			if (cc == '\n' && lastc == '\r')
			    ;	// do nothing, CR was already escaped
			else
			    sb.append('\\');	// Escape the character
		    sb.append(cc);
		    lastc = cc;
		}
		sb.append('"');
		return sb.toString();
	    } else if (c < 040 || (c >= 0177 && !allowUtf8) ||
		    specials.indexOf(c) >= 0)
		// These characters cause the string to be quoted
		needQuoting = true;
	}
	
	if (needQuoting) {
	    StringBuilder sb = new StringBuilder(len + 2);
	    sb.append('"').append(word).append('"');
	    return sb.toString();
	} else 
	    return word;
	}

	/**
	 * Fold a string at linear whitespace so that each line is no longer
	 * than 76 characters, if possible.  If there are more than 76
	 * non-whitespace characters consecutively, the string is folded at
	 * the first whitespace after that sequence.  The parameter
	 * <code>used</code> indicates how many characters have been used in
	 * the current line; it is usually the length of the header name. <p>
	 *
	 * Note that line breaks in the string aren't escaped; they probably
	 * should be.
	 *
	 * @param	used	characters used in line so far
	 * @param	s	the string to fold
	 * @return		the folded string
	 * @since		JavaMail 1.4
	 */
	public static String fold(int used, String s) {
	if (!foldText)
	    return s;
	
	int end;
	char c;
	// Strip trailing spaces and newlines
	for (end = s.length() - 1; end >= 0; end--) {
	    c = s.charAt(end);
	    if (c != ' ' && c != '\t' && c != '\r' && c != '\n')
		break;
	}
	if (end != s.length() - 1)
	    s = s.substring(0, end + 1);
	
	// if the string fits now, just return it
	if (used + s.length() <= 76)
	    return makesafe(s);
	
	// have to actually fold the string
	StringBuilder sb = new StringBuilder(s.length() + 4);
	char lastc = 0;
	while (used + s.length() > 76) {
	    int lastspace = -1;
	    for (int i = 0; i < s.length(); i++) {
		if (lastspace != -1 && used + i > 76)
		    break;
		c = s.charAt(i);
		if (c == ' ' || c == '\t')
		    if (!(lastc == ' ' || lastc == '\t'))
			lastspace = i;
		lastc = c;
	    }
	    if (lastspace == -1) {
		// no space, use the whole thing
		sb.append(s);
		s = "";
		used = 0;
		break;
	    }
	    sb.append(s.substring(0, lastspace));
	    sb.append("\r\n");
	    lastc = s.charAt(lastspace);
	    sb.append(lastc);
	    s = s.substring(lastspace + 1);
	    used = 1;
	}
	sb.append(s);
	return makesafe(sb);
	}

	/**
	 * If the String or StringBuilder has any embedded newlines,
	 * make sure they're followed by whitespace, to prevent header
	 * injection errors.
	 */
	private static String makesafe(CharSequence s) {
	int i;
	for (i = 0; i < s.length(); i++) {
	    char c = s.charAt(i);
	    if (c == '\r' || c == '\n')
		break;
	}
	if (i == s.length())	// went through whole string with no CR or LF
	    return s.toString();
	
	// read the lines in the string and reassemble them,
	// eliminating blank lines and inserting whitespace as necessary
	StringBuilder sb = new StringBuilder(s.length() + 1);
	BufferedReader r = new BufferedReader(new StringReader(s.toString()));
	String line;
	try {
	    while ((line = r.readLine()) != null) {
		if (line.trim().length() == 0)
		    continue;	// ignore empty lines
		if (sb.length() > 0) {
		    sb.append("\r\n");
		    assert line.length() > 0; // proven above
		    char c = line.charAt(0);
		    if (c != ' ' && c != '\t')
			sb.append(' ');
		}
		sb.append(line);
	    }
	} catch (IOException ex) {
	    // XXX - should never happen when reading from a string
	    return s.toString();
	}
	return sb.toString();
	}

	/**
     * Unfold a folded header.  Any line breaks that aren't escaped and
     * are followed by whitespace are removed.
     *
     * @param	s	the string to unfold
     * @return		the unfolded string
     * @since		JavaMail 1.4
     */
    public static String unfold(String s) {
	if (!foldText)
	    return s;

	StringBuilder sb = null;
	int i;
	while ((i = indexOfAny(s, "\r\n")) >= 0) {
	    int start = i;
	    int slen = s.length();
	    i++;		// skip CR or NL
	    if (i < slen && s.charAt(i - 1) == '\r' && s.charAt(i) == '\n')
		i++;	// skip LF
	    if (start > 0 && s.charAt(start - 1) == '\\') {
		// there's a backslash before the line break
		// strip it out, but leave in the line break
		if (sb == null)
		    sb = new StringBuilder(s.length());
		sb.append(s.substring(0, start - 1));
		sb.append(s.substring(start, i));
		s = s.substring(i);
	    } else {
		char c;
		// if next line starts with whitespace,
		// or at the end of the string, remove the line break
		// XXX - next line should always start with whitespace
		if (i >= slen || (c = s.charAt(i)) == ' ' || c == '\t') {
		    if (sb == null)
			sb = new StringBuilder(s.length());
		    sb.append(s.substring(0, start));
		    s = s.substring(i);
		} else {
		    // it's not a continuation line, just leave in the newline
		    if (sb == null)
			sb = new StringBuilder(s.length());
		    sb.append(s.substring(0, i));
		    s = s.substring(i);
		}
	    }
	}
	if (sb != null) {
	    sb.append(s);
	    return sb.toString();
	} else
	    return s;
    }

    /**
     * Return the first index of any of the characters in "any" in "s",
     * or -1 if none are found.
     *
     * This should be a method on String.
     */
    private static int indexOfAny(String s, String any) {
	return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
	try {
	    int len = s.length();
	    for (int i = start; i < len; i++) {
		if (any.indexOf(s.charAt(i)) >= 0)
		    return i;
	    }
	    return -1;
	} catch (StringIndexOutOfBoundsException e) {
	    return -1;
	}
    }

    /**
     * Convert a MIME charset name into a valid Java charset name. <p>
     *
     * @param charset	the MIME charset name
     * @return  the Java charset equivalent. If a suitable mapping is
     *		not available, the passed in charset is itself returned.
     */
    public static String javaCharset(String charset) {
	if (mime2java == null || charset == null)
	    // no mapping table, or charset parameter is null
	    return charset;

	String alias = mime2java.get(charset.toLowerCase(Locale.ENGLISH));
	if (alias != null) {
	    // verify that the mapped name is valid before trying to use it
	    try {
		Charset.forName(alias);
	    } catch (Exception ex) {
		alias = null;	// charset alias not valid, use original name
	    }
	}
	return alias == null ? charset : alias;
    }

    /**
     * Convert a java charset into its MIME charset name. <p>
     *
     * Note that a future version of JDK (post 1.2) might provide
     * this functionality, in which case, we may deprecate this
     * method then.
     *
     * @param   charset    the JDK charset
     * @return      	the MIME/IANA equivalent. If a mapping
     *			is not possible, the passed in charset itself
     *			is returned.
     * @since		JavaMail 1.1
     */
    public static String mimeCharset(String charset) {
	if (java2mime == null || charset == null) 
	    // no mapping table or charset param is null
	    return charset;

	String alias = java2mime.get(charset.toLowerCase(Locale.ENGLISH));
	return alias == null ? charset : alias;
    }

    private static String defaultJavaCharset;
    private static String defaultMIMECharset;

    /**
     * Get the default charset corresponding to the system's current 
     * default locale.  If the System property <code>mail.mime.charset</code>
     * is set, a system charset corresponding to this MIME charset will be
     * returned. <p>
     * 
     * @return	the default charset of the system's default locale, 
     * 		as a Java charset. (NOT a MIME charset)
     * @since	JavaMail 1.1
     */
    public static String getDefaultJavaCharset() {
	if (defaultJavaCharset == null) {
	    /*
	     * If mail.mime.charset is set, it controls the default
	     * Java charset as well.
	     */
	    String mimecs = null;
	    try {
		mimecs = System.getProperty("mail.mime.charset");
	    } catch (SecurityException ex) { }	// ignore it
	    if (mimecs != null && mimecs.length() > 0) {
		defaultJavaCharset = javaCharset(mimecs);
		return defaultJavaCharset;
	    }

	    try {
		defaultJavaCharset = System.getProperty("file.encoding", 
							"8859_1");
	    } catch (SecurityException sex) {
		
		class NullInputStream extends InputStream {
		    @Override
		    public int read() {
			return 0;
		    }
		}
		InputStreamReader reader = 
			new InputStreamReader(new NullInputStream());
		defaultJavaCharset = reader.getEncoding();
		if (defaultJavaCharset == null)
		    defaultJavaCharset = "8859_1";
	    }
	}

	return defaultJavaCharset;
    }

    /*
     * Get the default MIME charset for this locale.
     */
    static String getDefaultMIMECharset() {
	if (defaultMIMECharset == null) {
	    try {
		defaultMIMECharset = System.getProperty("mail.mime.charset");
	    } catch (SecurityException ex) { }	// ignore it
	}
	if (defaultMIMECharset == null)
	    defaultMIMECharset = mimeCharset(getDefaultJavaCharset());
	return defaultMIMECharset;
    }

    // Tables to map MIME charset names to Java names and vice versa.
    // XXX - Should eventually use J2SE 1.4 java.nio.charset.Charset
    private static Map<String, String> mime2java;
    private static Map<String, String> java2mime;

    static {
	java2mime = new HashMap<>(40);
	mime2java = new HashMap<>(14);

	try {
	    // Use this class's classloader to load the mapping file
	    // XXX - we should use SecuritySupport, but it's in another package
	    InputStream is = 
		    org.simplejavamail.jakarta.mail.internet.MimeUtility.class.getResourceAsStream(
		    "/META-INF/javamail.charset.map");

	    if (is != null) {
		try {
		    is = new LineInputStream(is);

		    // Load the JDK-to-MIME charset mapping table
		    loadMappings((LineInputStream)is, java2mime);

		    // Load the MIME-to-JDK charset mapping table
		    loadMappings((LineInputStream)is, mime2java);
		} finally {
		    try {
			is.close();
		    } catch (Exception cex) {
			// ignore
		    }
		}
	    }
	} catch (Exception ex) { }

	// If we didn't load the tables, e.g., because we didn't have
	// permission, load them manually.  The entries here should be
	// the same as the default javamail.charset.map.
	if (java2mime.isEmpty()) {
	    java2mime.put("8859_1", "ISO-8859-1");
	    java2mime.put("iso8859_1", "ISO-8859-1");
	    java2mime.put("iso8859-1", "ISO-8859-1");

	    java2mime.put("8859_2", "ISO-8859-2");
	    java2mime.put("iso8859_2", "ISO-8859-2");
	    java2mime.put("iso8859-2", "ISO-8859-2");

	    java2mime.put("8859_3", "ISO-8859-3");
	    java2mime.put("iso8859_3", "ISO-8859-3");
	    java2mime.put("iso8859-3", "ISO-8859-3");

	    java2mime.put("8859_4", "ISO-8859-4");
	    java2mime.put("iso8859_4", "ISO-8859-4");
	    java2mime.put("iso8859-4", "ISO-8859-4");

	    java2mime.put("8859_5", "ISO-8859-5");
	    java2mime.put("iso8859_5", "ISO-8859-5");
	    java2mime.put("iso8859-5", "ISO-8859-5");

	    java2mime.put("8859_6", "ISO-8859-6");
	    java2mime.put("iso8859_6", "ISO-8859-6");
	    java2mime.put("iso8859-6", "ISO-8859-6");

	    java2mime.put("8859_7", "ISO-8859-7");
	    java2mime.put("iso8859_7", "ISO-8859-7");
	    java2mime.put("iso8859-7", "ISO-8859-7");

	    java2mime.put("8859_8", "ISO-8859-8");
	    java2mime.put("iso8859_8", "ISO-8859-8");
	    java2mime.put("iso8859-8", "ISO-8859-8");

	    java2mime.put("8859_9", "ISO-8859-9");
	    java2mime.put("iso8859_9", "ISO-8859-9");
	    java2mime.put("iso8859-9", "ISO-8859-9");

	    java2mime.put("sjis", "Shift_JIS");
	    java2mime.put("jis", "ISO-2022-JP");
	    java2mime.put("iso2022jp", "ISO-2022-JP");
	    java2mime.put("euc_jp", "euc-jp");
	    java2mime.put("koi8_r", "koi8-r");
	    java2mime.put("euc_cn", "euc-cn");
	    java2mime.put("euc_tw", "euc-tw");
	    java2mime.put("euc_kr", "euc-kr");
	}
	if (mime2java.isEmpty()) {
	    mime2java.put("iso-2022-cn", "ISO2022CN");
	    mime2java.put("iso-2022-kr", "ISO2022KR");
	    mime2java.put("utf-8", "UTF8");
	    mime2java.put("utf8", "UTF8");
	    mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
	    mime2java.put("ja_jp.eucjp", "EUCJIS");
	    mime2java.put("euc-kr", "KSC5601");
	    mime2java.put("euckr", "KSC5601");
	    mime2java.put("us-ascii", "ISO-8859-1");
	    mime2java.put("x-us-ascii", "ISO-8859-1");
	    mime2java.put("gb2312", "GB18030");
	    mime2java.put("cp936", "GB18030");
	    mime2java.put("ms936", "GB18030");
	    mime2java.put("gbk", "GB18030");
	}
    }

    private static void loadMappings(LineInputStream is,
	    Map<String, String> table) {
	String currLine;

	while (true) {
	    try {
		currLine = is.readLine();
	    } catch (IOException ioex) {
		break; // error in reading, stop
	    }

	    if (currLine == null) // end of file, stop
		break;
	    if (currLine.startsWith("--") && currLine.endsWith("--"))
		// end of this table
		break;	

	    // ignore empty lines and comments
	    if (currLine.trim().length() == 0 || currLine.startsWith("#"))
		continue;
	    
	    // A valid entry is of the form <key><separator><value>
	    // where, <separator> := SPACE | HT. Parse this
	    StringTokenizer tk = new StringTokenizer(currLine, " \t");
	    try {
		String key = tk.nextToken();
		String value = tk.nextToken();
		table.put(key.toLowerCase(Locale.ENGLISH), value);
	    } catch (NoSuchElementException nex) { }
	}
    }

    static final int ALL_ASCII 		= 1;
    static final int MOSTLY_ASCII 	= 2;
    static final int MOSTLY_NONASCII 	= 3;

    /** 
     * Check if the given string contains non US-ASCII characters.
     * @param	s	string
     * @return		ALL_ASCII if all characters in the string 
     *			belong to the US-ASCII charset. MOSTLY_ASCII
     *			if more than half of the available characters
     *			are US-ASCII characters. Else MOSTLY_NONASCII.
     */
    static int checkAscii(String s) {
	int ascii = 0, non_ascii = 0;
	int l = s.length();

	for (int i = 0; i < l; i++) {
	    if (nonascii((int)s.charAt(i))) // non-ascii
		non_ascii++;
	    else
		ascii++;
	}

	if (non_ascii == 0)
	    return ALL_ASCII;
	if (ascii > non_ascii)
	    return MOSTLY_ASCII;

	return MOSTLY_NONASCII;
    }

    /** 
     * Check if the given byte array contains non US-ASCII characters.
     * @param	b	byte array
     * @return		ALL_ASCII if all characters in the string 
     *			belong to the US-ASCII charset. MOSTLY_ASCII
     *			if more than half of the available characters
     *			are US-ASCII characters. Else MOSTLY_NONASCII.
     *
     * XXX - this method is no longer used
     */
    static int checkAscii(byte[] b) {
	int ascii = 0, non_ascii = 0;

	for (int i=0; i < b.length; i++) {
	    // The '&' operator automatically causes b[i] to be promoted
	    // to an int, and we mask out the higher bytes in the int 
	    // so that the resulting value is not a negative integer.
	    if (nonascii(b[i] & 0xff)) // non-ascii
		non_ascii++;
	    else
		ascii++;
	}
	
	if (non_ascii == 0)
	    return ALL_ASCII;
	if (ascii > non_ascii)
	    return MOSTLY_ASCII;
	
	return MOSTLY_NONASCII;
    }

    /** 
     * Check if the given input stream contains non US-ASCII characters.
     * Upto <code>max</code> bytes are checked. If <code>max</code> is
     * set to <code>ALL</code>, then all the bytes available in this
     * input stream are checked. If <code>breakOnNonAscii</code> is true
     * the check terminates when the first non-US-ASCII character is
     * found and MOSTLY_NONASCII is returned. Else, the check continues
     * till <code>max</code> bytes or till the end of stream.
     *
     * @param	is	the input stream
     * @param	max	maximum bytes to check for. The special value
     *			ALL indicates that all the bytes in this input
     *			stream must be checked.
     * @param	breakOnNonAscii if <code>true</code>, then terminate the
     *			the check when the first non-US-ASCII character
     *			is found.
     * @return		ALL_ASCII if all characters in the string 
     *			belong to the US-ASCII charset. MOSTLY_ASCII
     *			if more than half of the available characters
     *			are US-ASCII characters. Else MOSTLY_NONASCII.
     */
    static int checkAscii(InputStream is, int max, boolean breakOnNonAscii) {
	int ascii = 0, non_ascii = 0;
	int len;
	int block = 4096;
	int linelen = 0;
	boolean longLine = false, badEOL = false;
	boolean checkEOL = encodeEolStrict && breakOnNonAscii;
	byte buf[] = null;
	if (max != 0) {
	    block = (max == ALL) ? 4096 : Math.min(max, 4096);
	    buf = new byte[block]; 
	}
	while (max != 0) {
	    try {
		if ((len = is.read(buf, 0, block)) == -1)
		    break;
		int lastb = 0;
		for (int i = 0; i < len; i++) {
	    	    // The '&' operator automatically causes b[i] to 
		    // be promoted to an int, and we mask out the higher
		    // bytes in the int so that the resulting value is 
		    // not a negative integer.
		    int b = buf[i] & 0xff;
		    if (checkEOL &&
			    ((lastb == '\r' && b != '\n') ||
			    (lastb != '\r' && b == '\n')))
			badEOL = true;
		    if (b == '\r' || b == '\n')
			linelen = 0;
		    else {
			linelen++;
			if (linelen > 998)	// 1000 - CRLF
			    longLine = true;
		    }
		    if (nonascii(b)) {	// non-ascii
		        if (breakOnNonAscii) // we are done
			    return MOSTLY_NONASCII;
		        else
			    non_ascii++;
		    } else
		        ascii++;
		    lastb = b;
		}
	    } catch (IOException ioex) {
		break;
	    }
	    if (max != ALL)
		max -= len;
	}

	if (max == 0 && breakOnNonAscii)
	    // We have been told to break on the first non-ascii character.
	    // We haven't got any non-ascii character yet, but then we
	    // have not checked all of the available bytes either. So we
	    // cannot say for sure that this input stream is ALL_ASCII,
	    // and hence we must play safe and return MOSTLY_NONASCII

	    return MOSTLY_NONASCII;

	if (non_ascii == 0) { // no non-us-ascii characters so far
	    // If we're looking at non-text data, and we saw CR without LF
	    // or vice versa, consider this mostly non-ASCII so that it
	    // will be base64 encoded (since the quoted-printable encoder
	    // doesn't encode this case properly).
	    if (badEOL)
		return MOSTLY_NONASCII;
	    // if we've seen a long line, we degrade to mostly ascii
	    else if (longLine)
		return MOSTLY_ASCII;
	    else
		return ALL_ASCII;
	}
	if (ascii > non_ascii) // mostly ascii
	    return MOSTLY_ASCII;
	return MOSTLY_NONASCII;
    }

    static final boolean nonascii(int b) {
	return b >= 0177 || (b < 040 && b != '\r' && b != '\n' && b != '\t');
    }
}

/**
 * An OutputStream that determines whether the data written to
 * it is all ASCII, mostly ASCII, or mostly non-ASCII.
 */
class AsciiOutputStream extends OutputStream {
    private boolean breakOnNonAscii;
    private int ascii = 0, non_ascii = 0;
    private int linelen = 0;
    private boolean longLine = false;
    private boolean badEOL = false;
    private boolean checkEOL = false;
    private int lastb = 0;
    private int ret = 0;

    public AsciiOutputStream(boolean breakOnNonAscii, boolean encodeEolStrict) {
	this.breakOnNonAscii = breakOnNonAscii;
	checkEOL = encodeEolStrict && breakOnNonAscii;
    }

    @Override
    public void write(int b) throws IOException {
	check(b);
    }

    @Override
    public void write(byte b[]) throws IOException {
	write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
	len += off;
	for (int i = off; i < len ; i++)
	    check(b[i]);
    }

    private final void check(int b) throws IOException {
	b &= 0xff;
	if (checkEOL &&
		((lastb == '\r' && b != '\n') || (lastb != '\r' && b == '\n')))
	    badEOL = true;
	if (b == '\r' || b == '\n')
	    linelen = 0;
	else {
	    linelen++;
	    if (linelen > 998)	// 1000 - CRLF
		longLine = true;
	}
	if (MimeUtility.nonascii(b)) { // non-ascii
	    non_ascii++;
	    if (breakOnNonAscii) {	// we are done
		ret = MimeUtility.MOSTLY_NONASCII;
		throw new EOFException();
	    }
	} else
	    ascii++;
	lastb = b;
    }

    /**
     * Return ASCII-ness of data stream.
     */
    public int getAscii() {
	if (ret != 0)
	    return ret;
	// If we're looking at non-text data, and we saw CR without LF
	// or vice versa, consider this mostly non-ASCII so that it
	// will be base64 encoded (since the quoted-printable encoder
	// doesn't encode this case properly).
	if (badEOL)
	    return MimeUtility.MOSTLY_NONASCII;
	else if (non_ascii == 0) { // no non-us-ascii characters so far
	    // if we've seen a long line, we degrade to mostly ascii
	    if (longLine)
		return MimeUtility.MOSTLY_ASCII;
	    else
		return MimeUtility.ALL_ASCII;
	}
	if (ascii > non_ascii) // mostly ascii
	    return MimeUtility.MOSTLY_ASCII;
	return MimeUtility.MOSTLY_NONASCII;
    }
}
