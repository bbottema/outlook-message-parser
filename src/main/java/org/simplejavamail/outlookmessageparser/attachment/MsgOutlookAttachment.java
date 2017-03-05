package org.simplejavamail.outlookmessageparser.attachment;

import org.simplejavamail.outlookmessageparser.OutlookMessage;

/**
 * This {@link OutlookAttachment} implementation represents a .msg object attachment. Instead of storing a byte[] of the attachment, this implementation provides an
 * embedded {@link OutlookMessage} object.
 */
public class MsgOutlookAttachment implements OutlookAttachment {

	/**
	 * The encapsulated (attached) outlookMessage.
	 */
	private OutlookMessage outlookMessage = null;

	@Override
	public String toString() {
		if (this.outlookMessage == null) {
			return null;
		}
		return "Mail OutlookAttachment: " + this.outlookMessage.toString();
	}

	/**
	 * Bean getter for {@link #outlookMessage}.
	 */
	public OutlookMessage getOutlookMessage() {
		return outlookMessage;
	}

	/**
	 * Bean setter for {@link #outlookMessage}.
	 */
	public void setOutlookMessage(OutlookMessage outlookMessage) {
		this.outlookMessage = outlookMessage;
	}
}
