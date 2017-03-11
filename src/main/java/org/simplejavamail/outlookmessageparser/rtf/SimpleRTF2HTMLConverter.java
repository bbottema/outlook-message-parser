package org.simplejavamail.outlookmessageparser.rtf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

/**
 * This class is intended to be used for certain RTF related operations such as extraction of plain HTML from an RTF text.
 */
public class SimpleRTF2HTMLConverter implements RTF2HTMLConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRTF2HTMLConverter.class);

	private static final String[] HTML_START_TAGS = { "<html ", "<Html ", "<HTML " };
	private static final String[] HTML_END_TAGS = { "</html>", "</Html>", "</HTML>" };
	private static final String WINDOWS_CHARSET = "CP1252";

	public String rtf2html(final String rtf) {
		if (rtf != null) {
			String plain = fetchHtmlSection(rtf);
			plain = replaceHexSequences(plain);
			plain = replaceSpecialSequences(plain);
			plain = replaceRemainingControlSequences(plain);
			plain = replaceLineBreaks(plain);
			return plain;
		}
		return null;
	}

	/**
	 * @return The text with removed newlines as they are only part of the RTF document and should not be inside the HTML.
	 */
	private String replaceLineBreaks(final String text) {
		String replacedText = text;
		replacedText = replacedText.replaceAll("( <br/> ( <br/> )+)", " <br/> ");
		replacedText = replacedText.replaceAll("[\\n\\r]+", "");
		return replacedText;
	}

	/**
	 * @return The text with replaced special characters that denote hex codes for strings using Windows CP1252 encoding.
	 */
	private String replaceHexSequences(final String text) {
		final Matcher m = compile("\\\\'(..)").matcher(text);

		String replacedText = text;
		while (m.find()) {
			for (int g = 1; g <= m.groupCount(); g++) {
				final String hex = m.group(g);
				final String hexToString = hexToString(hex);
				if (hexToString != null) {
					replacedText = replacedText.replaceAll("\\\\'" + hex, hexToString);
				}
			}
		}

		return replacedText;
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
			html = html.replaceAll("[\\n\\r]+", " <br/> ");
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
		//filtering rtf style headers like {\f0\fswiss\fcharset0 Arial;}
		replacedText = replacedText.replaceAll("\\{\\\\f\\d+[^\\}]*\\}", "");
		//filtering embedded tags like {\*\htmltag64 <tr>}                                          }
		replacedText = replacedText.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+(<.+>)\\}", "$1");
		//filtering embedded tags like {\*\htmltag84 &#43;}
		replacedText = replacedText.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+\\}", "");
		//filtering curly braces that are NOT escaped with backslash },
		//thus marking the end of an RTF sequence
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\}+", "$1");
		replacedText = replacedText.replaceAll("([^\\\\])" + "\\{+", "$1");
		//filtering curly braces that are escaped with backslash \},
		//thus representing an actual brace
		replacedText = replacedText.replaceAll("\\\\\\}", "}");
		replacedText = replacedText.replaceAll("\\\\\\{", "{");
		return replacedText;
	}

	/**
	 * @return The string representing the hex value of a special character.
	 */
	private static String hexToString(final String hex) {
		final int i;
		try {
			i = Integer.parseInt(hex, 16);
		} catch (final NumberFormatException nfe) {
			LOGGER.warn("Could not interpret {} as a number.", hex, nfe);
			return null;
		}
		try {
			return new String(new byte[] { (byte) i }, WINDOWS_CHARSET);
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding: {}", WINDOWS_CHARSET, e);
		}
		return null;
	}

	/**
	 * @return The text with all control sequences replaced, such as line breaks with plain text breaks or equivalent representations.
	 */
	private String replaceRemainingControlSequences(final String text) {
		String replacedText = text;
		//filtering \par sequences
		replacedText = replacedText.replaceAll("\\\\pard*", "\n");
		//filtering \tab sequences
		replacedText = replacedText.replaceAll("\\\\tab", "\t");
		//filtering \*\<rtfsequence> like e.g.: \*\fldinst
		replacedText = replacedText.replaceAll("\\\\\\*\\\\\\S+", "");
		//filtering \<rtfsequence> like e.g.: \htmlrtf
		replacedText = replacedText.replaceAll("\\\\\\S+", "");
		return replacedText;
	}
}