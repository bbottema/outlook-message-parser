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
package org.simplejavamail.outlookmessageparser.model;

import javax.activation.MimetypesFileTypeMap;
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