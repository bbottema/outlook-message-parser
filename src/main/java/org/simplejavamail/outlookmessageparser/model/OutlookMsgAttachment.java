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

	public OutlookMsgAttachment(OutlookMessage outlookMessage) {
		this.outlookMessage = outlookMessage;
	}

	@Override
	public String toString() {
		return outlookMessage.toString();
	}

	/**
	 * Bean getter for {@link #outlookMessage}.
	 */
	public OutlookMessage getOutlookMessage() {
		return outlookMessage;
	}
}