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

	protected static final Logger LOGGER = LoggerFactory.getLogger(SimpleRTF2HTMLConverter.class);

	private static final String[] HTML_START_TAGS = new String[] { "<html ", "<Html ", "<HTML " };
	private static final String[] HTML_END_TAGS = new String[] { "</html>", "</Html>", "</HTML>" };
	private static final String WINDOWS_CHARSET = "CP1252";

	public String rtf2html(String rtf) {
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
	private String replaceLineBreaks(String text) {
		text = text.replaceAll("( <br/> ( <br/> )+)", " <br/> ");
		text = text.replaceAll("[\\n\\r]+", "");
		return text;
	}

	/**
	 * @return The text with replaced special characters that denote hex codes for strings using Windows CP1252 encoding.
	 */
	private String replaceHexSequences(String text) {
		Matcher m = compile("\\\\'(..)").matcher(text);

		while (m.find()) {
			for (int g = 1; g <= m.groupCount(); g++) {
				String hex = m.group(g);
				String hexToString = hexToString(hex);
				if (hexToString != null) {
					text = text.replaceAll("\\\\'" + hex, hexToString);
				}
			}
		}

		return text;
	}

	/**
	 * @return The actual HTML block / section only but still with RTF code inside (still needs to be cleaned).
	 */
	private String fetchHtmlSection(String text) {
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
	private String replaceSpecialSequences(String text) {
		//filtering whatever color control sequence, e.g. {\sp{\sn fillColor}{\sv 14935011}}{\sp{\sn fFilled}{\sv 1}}
		text = text.replaceAll("\\{\\\\S+ [^\\s\\\\}]*\\}", "");
		//filtering hyperlink sequences like {HYPERLINK "http://xyz.com/print.jpg"}
		text = text.replaceAll("\\{HYPERLINK[^\\}]*\\}", "");
		//filtering plain text sequences like {\pntext *\tab}
		text = text.replaceAll("\\{\\\\pntext[^\\}]*\\}", "");
		//filtering rtf style headers like {\f0\fswiss\fcharset0 Arial;}
		text = text.replaceAll("\\{\\\\f\\d+[^\\}]*\\}", "");
		//filtering embedded tags like {\*\htmltag64 <tr>}                                          }
		text = text.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+(<.+>)\\}", "$1");
		//filtering embedded tags like {\*\htmltag84 &#43;}
		text = text.replaceAll("\\{\\\\\\*\\\\htmltag\\d+[^\\}<]+\\}", "");
		//filtering curly braces that are NOT escaped with backslash },
		//thus marking the end of an RTF sequence
		text = text.replaceAll("([^\\\\])" + "\\}+", "$1");
		text = text.replaceAll("([^\\\\])" + "\\{+", "$1");
		//filtering curly braces that are escaped with backslash \},
		//thus representing an actual brace
		text = text.replaceAll("\\\\\\}", "}");
		text = text.replaceAll("\\\\\\{", "{");
		return text;
	}

	/**
	 * @return The string representing the hex value of a special character.
	 */
	private static String hexToString(String hex) {
		final int i;
		try {
			i = Integer.parseInt(hex, 16);
		} catch (NumberFormatException nfe) {
			LOGGER.warn("Could not interpret {} as a number.", hex, nfe);
			return null;
		}
		try {
			return new String(new byte[] { (byte) i }, WINDOWS_CHARSET);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding: {}", WINDOWS_CHARSET, e);
		}
		return null;
	}

	/**
	 * @return The text with all control sequences replaced, such as line breaks with plain text breaks or equivalent representations.
	 */
	private String replaceRemainingControlSequences(String text) {
		//filtering \par sequences
		text = text.replaceAll("\\\\pard*", "\n");
		//filtering \tab sequences
		text = text.replaceAll("\\\\tab", "\t");
		//filtering \*\<rtfsequence> like e.g.: \*\fldinst
		text = text.replaceAll("\\\\\\*\\\\\\S+", "");
		//filtering \<rtfsequence> like e.g.: \htmlrtf
		text = text.replaceAll("\\\\\\S+", "");
		return text;
	}
}