package org.simplejavamail.outlookmessageparser.rtf;

/**
 * This interface defines the structure of the conversion class to be used for extracting HTML code out of an RTF body in case no pure HTML body was found. By
 * default the msgparser uses the built-in {@link SimpleRTF2HTMLConverter} class but it can be replaced by a custom implementation.
 */
public interface RTF2HTMLConverter {
	String rtf2html(String rtf);
}