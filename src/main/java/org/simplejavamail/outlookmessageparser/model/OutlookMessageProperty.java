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

/**
 * Represents a message property holding the type of data and the data itself.
 */
public class OutlookMessageProperty {

	/**
	 * A 4 digit code representing the property type.
	 */
	private final String clazz;
	private final Object data;
	private final int size;

	public OutlookMessageProperty(final String clazz, final Object data, final int size) {
		this.clazz = clazz;
		this.data = data;
		this.size = size;
	}

	/**
	 * Bean getter for {@link #clazz}.
	 */
	public String getClazz() {
		return clazz;
	}

	public Object getData() {
		return data;
	}

	public int getSize() {
		return size;
	}
}