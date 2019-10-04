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
package org.simplejavamail.outlookmessageparser.rtf;

import org.simplejavamail.outlookmessageparser.rtf.util.CharsetHelper;

import java.nio.charset.Charset;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;
import static org.simplejavamail.outlookmessageparser.rtf.util.ByteUtil.hexToString;
import static org.simplejavamail.outlookmessageparser.rtf.util.CharsetHelper.WINDOWS_CHARSET;

/**
 * This class is intended to be used for certain RTF related operations such as extraction of plain HTML from an RTF text.
 */
public class SimpleRTF2HTMLConverter implements RTF2HTMLConverter {

	private static final String[] HTML_START_TAGS = { "<html", "<Html", "<HTML" };
	private static final String[] HTML_END_TAGS = { "</html>", "</Html>", "</HTML>" };
	
	public String rtf2html(final String rtf) {
		if (rtf != null) {
			final Charset charset = extractCodepage(rtf);
			String plain = fetchHtmlSection(rtf);
			plain = replaceSpecialSequences(plain); // first step, remove known control words or else we'll match single escape hex values in the next step
			plain = replaceHexSequences(plain, "(?:\\\\f\\d(?:\\\\'..)+)", WINDOWS_CHARSET); // match all header control values with default charset
			plain = replaceHexSequences(plain, "(?:\\\\'..)+", charset); // match all remaining escaped hex values as encoded text (which might be DBCS like CP936)
			plain = cleanupRemainingSequences(plain);
			plain = replaceLineBreaks(plain);
			return plain;
		}
		return null;
	}
	
	private String cleanupRemainingSequences(String plain) {
		return plain
				.replaceAll("(\\\\f\\d.+?;)+", "") // clear all \f sequences including fontnames like Courier new
				.replaceAll("\\\\\\S+", "") // filtering all remaining \<rtfsequence> like e.g.: \htmlrtf
				.replaceAll("BM__MailAutoSig((?s).*?(?-s))BM__MailAutoSig", "$1");
	}
	
	private Charset extractCodepage(String rtf) {
		Matcher codePageMatcher = compile("(?:\\\\ansicpg(?<codePage>.+?)\\\\)+").matcher(rtf);
		if (codePageMatcher.find()) {
			return CharsetHelper.findCharset(codePageMatcher.group("codePage"));
		} else {
			return WINDOWS_CHARSET; // fallback
		}
	}
	
	/**
	 * @return The text with removed newlines as they are only part of the RTF document and should not be inside the HTML.
	 */
	private String replaceLineBreaks(final String text) {
		return text
				.replaceAll("( <br/> ( <br/> )+)", " <br/> ")
				.replaceAll("\\r\\n", "\n")
				.replaceAll("[\\r\\u0000]", "");
	}

	/**
	 * @return The text with replaced special characters that denote hex codes for strings using Windows CP1252 encoding.
	 */
	private String replaceHexSequences(final String text, String sequencesToMatch, final Charset charset) {
		final StringBuilder res = new StringBuilder();
		int lastPosition = 0;
		
	  	final Matcher escapedHexGroupMatcher = compile(sequencesToMatch).matcher(text);
		while (escapedHexGroupMatcher.find()) {
			res.append(text, lastPosition, escapedHexGroupMatcher.start());
			
			StringBuilder hexText = new StringBuilder();
			
			String escapedHexGroup = escapedHexGroupMatcher.group(0);
			final Matcher unescapedHexCharacterMatcher = compile("\\\\'(..)").matcher(escapedHexGroup);
			while (unescapedHexCharacterMatcher.find()) {
				hexText.append(unescapedHexCharacterMatcher.group(1));
			}
			
			res.append(hexToString(hexText.toString(), charset));
			
			lastPosition = escapedHexGroupMatcher.end();
		}
		
		if (res.length() == 0) {
			res.append(text);
		} else {
			res.append(text, lastPosition, text.length());
		}
		
		return res.toString();
	}

	/**
	 * @return The actual HTML block / section only but still with RTF code inside (still needs to be cleaned).
	 */
	private String fetchHtmlSection(final String text) {
		int htmlStart = -1;
		int htmlEnd = -1;

		//determine html tags
		for (int i = 0; i < HTML_START_TAGS.length && htmlStart < 0; i++) {
			htmlStart = text.indexOf(HTML_START_TAGS[i]);
		}
		for (int i = 0; i < HTML_END_TAGS.length && htmlEnd < 0; i++) {
			htmlEnd = text.indexOf(HTML_END_TAGS[i]);
			if (htmlEnd > 0) {
				htmlEnd = htmlEnd + HTML_END_TAGS[i].length();
			}
		}

		if (htmlStart > -1 && htmlEnd > -1) {
			//trim rtf code
			return text.substring(htmlStart, htmlEnd + 1);
		} else {
			//embed code within html tags
			String html = "<html><body style=\"font-family:'Courier',monospace;font-size:10pt;\">" + text + "</body></html>";
			//replace linebreaks with html breaks
			html = html.replaceAll("[\\n\\r]+", " ");
			//create hyperlinks
			html = html.replaceAll("(http://\\S+)", "<a href=\"$1\">$1</a>");
			return html.replaceAll("mailto:(\\S+@\\S+)", "<a href=\"mailto:$1\">$1</a>");
		}
	}

	/**
	 * @return The text with special sequences replaced by equivalent representations.
	 */
	private String replaceSpecialSequences(final String text) {
		String replacedText = text;
		//filtering whatever color control sequence, e.g. {\sp{\sn fillColor}{\sv 14935011}}{\sp{\sn fFilled}{\sv 1}}
		replacedText = replacedText.replaceAll("\\{\\\\S+ [^\\s\\\\}]*\\}", "");
		//filtering hyperlink sequences like {HYPERLINK "http://xyz.com/print.jpg"}
		replacedText = replacedText.replaceAll("\\{HYPERLINK[^\\}]*\\}", "");
		//filtering plain replacedText sequences like {\pntext *\tab}
		replacedText = replacedText.replaceAll("\\{\\\\pntext[^\\}]*\\}", "");
		//filtering embedded tags like {\*\htmltag84 &#43;}
		replacedText = replacedText.replaceAll("\\{\\\\\\*\\\\htmltag\\d+ (&[#\\w]+;)}\\\\htmlrtf.*\\\\htmlrtf0 ", "$1");
		//filtering curly braces that are NOT escaped with backslash }, thus marking the end of an RTF sequence
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\}+", "$1");
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\{+", "$1");
		//filtering curly braces that are escaped with backslash \}, thus representing an actual brace
		replacedText = replacedText.replaceAll("\\\\\\}", "}");
		replacedText = replacedText.replaceAll("\\\\\\{", "{");
		//filtering \par sequences
		replacedText = replacedText.replaceAll("\\\\pard*", "\n");
		//filtering \tab sequences
		replacedText = replacedText.replaceAll("\\\\tab", "\t");
		//filtering \*\<rtfsequence> like e.g.: \*\fldinst
		replacedText = replacedText.replaceAll("\\\\\\*\\\\\\S+", "");
		return replacedText;
	}
}