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
 * This {@link OutlookAttachment} implementation represents a .msg object attachment. Instead of storing a byte[] of the attachment, this implementation
 * provides an embedded {@link OutlookMessage} object.
 */
public class OutlookMsgAttachment implements OutlookAttachment {

	/**
	 * The encapsulated (attached) outlookMessage.
	 */
	private final OutlookMessage outlookMessage;

	public OutlookMsgAttachment(final OutlookMessage outlookMessage) {
		this.outlookMessage = outlookMessage;
	}

	@Override
	public String toString() {
		return outlookMessage.toString();
	}

	/**
	 * Bean getter for {@link #outlookMessage}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public OutlookMessage getOutlookMessage() {
		return outlookMessage;
	}
}