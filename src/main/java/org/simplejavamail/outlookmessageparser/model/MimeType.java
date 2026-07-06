package org.simplejavamail.outlookmessageparser.model;

import jakarta.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;

class MimeType {

    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = createMap();
    private static final String MIMETYPES_RESOURCE = "mimetypes.txt";

    /**
     * @return a vastly improved mimetype map
     */
    private static MimetypesFileTypeMap createMap() {
        return createMap(
                new ResourceLoader() {
                    @Override
                    public InputStream getResourceAsStream() {
                        return MimeType.class.getResourceAsStream("/" + MIMETYPES_RESOURCE);
                    }
                },
                new ResourceLoader() {
                    @Override
                    public InputStream getResourceAsStream() {
                        final ClassLoader classLoader = MimeType.class.getClassLoader();
                        return classLoader != null
                                ? classLoader.getResourceAsStream(MIMETYPES_RESOURCE)
                                : ClassLoader.getSystemResourceAsStream(MIMETYPES_RESOURCE);
                    }
                });
    }

    static MimetypesFileTypeMap createMap(final ResourceLoader... resourceLoaders) {
        for (ResourceLoader resourceLoader : resourceLoaders) {
            try (InputStream is = resourceLoader.getResourceAsStream()) {
                if (is != null) {
                    return new MimetypesFileTypeMap(is);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return new MimetypesFileTypeMap();
    }

    interface ResourceLoader {
        InputStream getResourceAsStream()
                throws IOException;
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
