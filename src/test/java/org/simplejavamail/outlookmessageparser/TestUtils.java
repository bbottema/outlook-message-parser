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
package org.simplejavamail.outlookmessageparser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
    public static String classpathFileToString(String classPathFile, Charset charset) {
        try {
            Path path = Paths.get(TestUtils.class.getResource(classPathFile).toURI());
            return Files.readString(path, charset);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public static String normalizeText(String text) {
        return text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
    }
}
