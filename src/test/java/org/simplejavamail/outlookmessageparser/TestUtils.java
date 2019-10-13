package org.simplejavamail.outlookmessageparser;

public class TestUtils {
    public static String normalizeText(String text) {
        return text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
