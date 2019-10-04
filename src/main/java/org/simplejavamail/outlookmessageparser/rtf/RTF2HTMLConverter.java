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

/**
 * This interface defines the structure of the conversion class to be used for extracting HTML code out of an RTF body in case no pure HTML body was found. By
 * default the msgparser uses the built-in {@link SimpleRTF2HTMLConverter} class but it can be replaced by a custom implementation.
 */
public interface RTF2HTMLConverter {
	String rtf2html(String rtf);
}