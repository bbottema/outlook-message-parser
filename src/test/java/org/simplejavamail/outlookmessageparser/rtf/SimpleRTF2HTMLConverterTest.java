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

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.simplejavamail.outlookmessageparser.TestUtils.classpathFileToString;
import static org.simplejavamail.outlookmessageparser.TestUtils.normalizeText;

public class SimpleRTF2HTMLConverterTest {

    @Test
    public void testConversion()  {
        SimpleRTF2HTMLConverter converter = new SimpleRTF2HTMLConverter();
        String rtf = classpathFileToString("/test-messages/rtf-to-html-input.rtf", UTF_8);
        String expectedHtml = classpathFileToString("/test-messages/rtf-to-html-output.html", UTF_8);

        String html = converter.rtf2html(rtf);
        assertEquals(normalizeText(expectedHtml), normalizeText(html));
    }

}