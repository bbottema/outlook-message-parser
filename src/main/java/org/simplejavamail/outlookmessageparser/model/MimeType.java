package org.simplejavamail.outlookmessageparser.model;

import jakarta.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;

class MimeType {

    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = createMap();

    /**
     * @return a vastly improved mimetype map
     */
    private static MimetypesFileTypeMap createMap() {
        try (InputStream is = MimeType.class.getClassLoader().getResourceAsStream("mimetypes.txt")) {
            return new MimetypesFileTypeMap(is);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getContentType(String fileName) {
        return getContentType(fileName, null);
    }

    public static String getContentType(String fileName, String charset) {
        String mimeType = MIMETYPES_FILE_TYPE_MAP.getContentType(fileName.toLowerCase());
        if (charset != null && (mimeType.startsWith("text/") || mimeType.contains("javascript"))) {
            mimeType += ";charset=" + charset.toLowerCase();
        }
        return mimeType;
    }
}
