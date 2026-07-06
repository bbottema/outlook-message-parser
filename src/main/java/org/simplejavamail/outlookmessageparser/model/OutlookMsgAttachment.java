package org.simplejavamail.outlookmessageparser.model;

import java.util.Objects;

/**
 * This {@link OutlookAttachment} implementation represents a .msg object attachment. Instead of storing a byte[] of the attachment, this implementation
 * provides an embedded {@link OutlookMessage} object.
 */
public class OutlookMsgAttachment implements OutlookAttachment {

	/**
	 * The encapsulated (attached) outlookMessage.
	 */
	private final OutlookMessage outlookMessage;
	/**
	 * The Outlook attachment properties that wrap the encapsulated message.
	 */
	private final OutlookFileAttachment attachment;

	public OutlookMsgAttachment(final OutlookMessage outlookMessage) {
		this(outlookMessage, new OutlookFileAttachment());
	}

	public OutlookMsgAttachment(final OutlookMessage outlookMessage, final OutlookFileAttachment attachment) {
		this.attachment = Objects.requireNonNull(attachment, "attachment");
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

	/**
	 * Bean getter for {@link #attachment}.
	 *
	 * @return Attachment-level metadata such as filename, content ID, and MIME type for this embedded Outlook message.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public OutlookFileAttachment getAttachment() {
		return attachment;
	}
}
