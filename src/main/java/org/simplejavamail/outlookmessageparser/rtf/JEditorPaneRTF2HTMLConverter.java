package org.simplejavamail.outlookmessageparser.rtf;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Uses Swing's JEditorPane to perform the HTML intepretation for RTF text.
 */
public class JEditorPaneRTF2HTMLConverter implements RTF2HTMLConverter {

	public String rtf2html(String rtf) {
		JEditorPane p = new JEditorPane();
		p.setContentType("text/rtf");
		EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
		try {
			kitRtf.read(new StringReader(rtf), p.getDocument(), 0);
			Writer writer = new StringWriter();
			EditorKit editorKitForContentType = p.getEditorKitForContentType("text/html");
			editorKitForContentType.write(writer, p.getDocument(), 0, p.getDocument().getLength());
			return writer.toString();
		} catch (IOException | BadLocationException e) {
			throw new RuntimeException("Could not convert RTF to HTML.", e);
		}
	}
}