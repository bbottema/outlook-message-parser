/*
 * msgparser - http://auxilii.com/msgparser
 * Copyright (C) 2007  Roman Kurmanowytsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.auxilii.msgparser;

import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.auxilii.msgparser.attachment.MsgAttachment;
import com.auxilii.msgparser.rtf.RTF2HTMLConverter;
import com.auxilii.msgparser.rtf.SimpleRTF2HTMLConverter;
import org.apache.poi.hmef.CompressedRTF;
import org.apache.poi.hsmf.datatypes.MAPIProperty;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents a .msg file. Some
 * fields from the .msg file are stored in special
 * parameters (e.g., {@link #fromEmail}). 
 * Attachments are stored in the property
 * {@link #attachments}). An attachment may be
 * of the type {@link MsgAttachment} which 
 * represents another attached (encapsulated)
 * .msg object.
 * 
 * @author roman.kurmanowytsch
 */
public class Message {
	protected static final Logger logger = Logger.getLogger(Message.class.getName());

	private static final String WINDOWS_CHARSET = "CP1252";

	/**
	 * The message class as defined in the .msg file.
	 */
	protected String messageClass = "IPM.Note";
	/**
	 * The message Id.
	 */
	protected String messageId = null;
	/**
	 * The address part of From: mail address.
	 */
	protected String fromEmail = null;
	/**
	 * The name part of the From: mail address
	 */
	protected String fromName = null;
	/**
	 * The address part of To: mail address.
	 */
	protected String toEmail = null;
	/**
	 * The name part of the To: mail address
	 */
	protected String toName = null;
	/**
	 * The mail's subject.
	 */
	protected String subject = null;
	/**
	 * The normalized body text.
	 */
	protected String bodyText = null;
	/**
	 * The displayed To: field
	 */
	protected String displayTo = null;
	/**
	 * The displayed Cc: field
	 */
	protected String displayCc = null;
	/**
	 * The displayed Bcc: field
	 */
	protected String displayBcc = null;
	
	/**
	 * The body in RTF format (if available)
	 */
	protected String bodyRTF = null;
	
	/**
	 * The body in HTML format (if available)
	 */
	protected String bodyHTML = null;
	
	/**
	 * The body in HTML format (converted from RTF)
	 */
	protected String convertedBodyHTML = null;
	/**
	 * Email headers (if available)
	 */
	protected String headers = null;
	
	/**
	 * Email Date
	 */
	protected Date date = null;
	
	/**
	 * Client Submit Time
	 */
	protected Date clientSubmitTime = null;

	protected Date creationDate = null;
	
	protected Date lastModificationDate = null;
	/**
	 * A list of all attachments (both {@link FileAttachment}
	 * and {@link MsgAttachment}).
	 */
	protected List<Attachment> attachments = new ArrayList<>();
	/**
	 * Contains all properties that are not
	 * covered by the special properties.
	 */
	protected final Map<Integer,Object> properties = new TreeMap<>();
	/**
	 * A list containing all recipients for this message 
	 * (which can be set in the 'to:', 'cc:' and 'bcc:' field, respectively).
	 */
	protected List<RecipientEntry> recipients = new ArrayList<>();
	protected final RTF2HTMLConverter rtf2htmlConverter;
	
	
	public Message() {
		this.rtf2htmlConverter = new SimpleRTF2HTMLConverter();
	}
	
	public Message(RTF2HTMLConverter rtf2htmlConverter) {
		if(rtf2htmlConverter != null) {
			this.rtf2htmlConverter = rtf2htmlConverter;
		} else {
			this.rtf2htmlConverter = new SimpleRTF2HTMLConverter();
		}
	}
	
	public void addAttachment(Attachment attachment) {
		this.attachments.add(attachment);
	}

	public void addRecipient(RecipientEntry recipient) {
		this.recipients.add(recipient);
		if(toEmail == null) {
			setToEmail(recipient.getToEmail());
		}
		if(toName == null) {
			setToName(recipient.getToName());
		}
	}
	
	
	/**
	 * Sets the name/value pair in the {@link #properties}
	 * map. Some properties are put into
	 * special attributes (e.g., {@link #toEmail} when
	 * the property name is '0076'). 
	 * 
	 * @throws ClassCastException Thrown if the detected data
	 *  type does not match the expected data type.
	 */
	public void setProperty(MessageProperty msgProp) throws ClassCastException {
		String name = msgProp.getClazz();
		Object value = msgProp.getData();

		if ((name == null) || (value == null)) {
			return;
		}

		//Most fields expect a String representation of the value
		String stringValue = this.convertValueToString(value);
		
		int mapiClass = -1;
		try {
			mapiClass = Integer.parseInt(name, 16);
		} catch (NumberFormatException e) {
			logger.log(Level.FINEST, "Unexpected type: "+name);  
		}
		
		switch(mapiClass) {
		case 0x1a: //MESSAGE CLASS
			this.setMessageClass(stringValue);
			break;
		case 0x1035:
			this.setMessageId(stringValue);
			break;
		case 0x37: //SUBJECT
		case 0xe1d: //NORMALIZED SUBJECT
			this.setSubject(stringValue);
			break;
		case 0xc1f: //SENDER EMAIL ADDRESS
		case 0x65: //SENT REPRESENTING EMAIL ADDRESS
		case 0x3ffa: //LAST MODIFIER NAME
		case 0x800d:
		case 0x8008:
			this.setFromEmail(stringValue);
			break;
		case 0x42: //SENT REPRESENTING NAME
			this.setFromName(stringValue);
			break;
		case 0x76: //RECEIVED BY EMAIL ADDRESS
			this.setToEmail(stringValue, true);
			break;
		case 0x8000:
			this.setToEmail(stringValue);
			break;
		case 0x3001: //DISPLAY NAME
			this.setToName(stringValue);
			break;
		case 0xe04: //DISPLAY TO
			this.setDisplayTo(stringValue);
			break;
		case 0xe03: //DISPLAY CC
			this.setDisplayCc(stringValue);
			break;
		case 0xe02: //DISPLAY BCC
			this.setDisplayBcc(stringValue);
			break;
		case 0x1013: //HTML
			this.setBodyHTML(stringValue, true);
			break;
		case 0x1000: //BODY
			this.setBodyText(stringValue);
			break;
		case 0x1009: //RTF COMPRESSED
			this.setBodyRTF(value);
			break;
		case 0x7d: //TRANSPORT MESSAGE HEADERS
			this.setHeaders(stringValue);
			break;
		case 0x3007: //CREATION TIME
			this.setCreationDate(stringValue);
			break;
		case 0x3008: //LAST MODIFICATION TIME
			this.setLastModificationDate(stringValue);
			break;
		case 0x39: //CLIENT SUBMIT TIME
			this.setClientSubmitTime(stringValue);
			break;
		}
		
		
		// save all properties (incl. those identified above)
		this.properties.put(mapiClass, value);
		
		checkToRecipient();
		
		// other possible values (some are duplicates)
		// 0044: recv name
		// 004d: author
		// 0050: reply
		// 005a: sender
		// 0065: sent email
		// 0076: received email
		// 0078: repr. email
		// 0c1a: sender name
		// 0e04: to
		// 0e1d: subject normalized
		// 1046: sender email
		// 3003: email address
		// 1008 rtf sync
	}
	

	protected String convertValueToString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return ((String) value);
		} else if (value instanceof byte[]) {
			try {
				return new String((byte[]) value, "CP1252");
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.FINE, "Unsupported encoding!", e);
				return null;
			}
		} else {
			logger.log(Level.FINE, "Unexpected body class: "+value.getClass().getName());
			return value.toString();
		}
	}

	/**
	 * Checks if the correct recipient's addresses are set.
	 */
	protected void checkToRecipient() {
		RecipientEntry toRecipient = getToRecipient();
		if(toRecipient != null) {
			setToEmail(toRecipient.getToEmail(), true);
			setToName(toRecipient.getToName());
			recipients.remove(toRecipient);
			recipients.add(0, toRecipient);
		}
	}

	/**
	 * @param date The date string to be converted (e.g.: 'Mon Jul 23 15:43:12 CEST 2012')
	 * @return A {@link Date} object representing the given date string.
	 */
	protected static Date parseDateString(String date) {
		//in order to parse the date we try using the US locale before we 
		//fall back to the default locale.
		List<SimpleDateFormat> sdfList = new ArrayList<>(2);
		sdfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US));
		sdfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"));
		
		Date d = null;
		for(SimpleDateFormat sdf : sdfList) {
			try {
				d = sdf.parse(date);
				if(d != null) {
					break;
				}
			} catch (ParseException e) {
				logger.log(Level.FINEST, "Unexpected date format for date "+date);  
			}
		}
		return d;
	}

	/**
	 * Decompresses compressed RTF data.
	 * @param value Data to be decompressed.
	 * @return A byte array representing the decompressed data.
	 */
	protected byte[] decompressRtfBytes(byte[] value) {
		byte[] decompressed = null;
		if(value != null) {
			try {
				CompressedRTF crtf = new CompressedRTF();
				decompressed = crtf.decompress(new ByteArrayInputStream(value));
			} catch(Exception e) {
				logger.log(Level.FINEST, "Could not decompress RTF data", e);
			}
		}
		return decompressed;
	}
	
	/**
	 * Provides a short representation of this .msg object.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("From: ").append(this.createMailString(this.fromEmail, this.fromName)).append("\n");
		sb.append("To: ").append(this.createMailString(this.toEmail, this.toName)).append("\n");
		if (this.date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			sb.append("Date: ").append(formatter.format(this.date)).append("\n");
		}
		if (this.subject != null) sb.append("Subject: ").append(this.subject).append("\n");
		sb.append("").append(this.attachments.size()).append(" attachments.");
		return sb.toString();
	}
	
	/**
	 * Provides all information of this message object.
	 * 
	 * @return The full message information.
	 */
	public String toLongString() {
		StringBuilder sb = new StringBuilder();
		sb.append("From: ").append(this.createMailString(this.fromEmail, this.fromName)).append("\n");
		sb.append("To: ").append(this.createMailString(this.toEmail, this.toName)).append("\n");
		if (this.date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			sb.append("Date: ").append(formatter.format(this.date)).append("\n");
		}
		if (this.subject != null) sb.append("Subject: ").append(this.subject).append("\n");
		sb.append("\n");
		if (this.bodyText != null) sb.append(this.bodyText);
		if (this.attachments.size() > 0) {
			sb.append("\n");
			sb.append("").append(this.attachments.size()).append(" attachments.\n");
			for (Attachment att : this.attachments) {
				sb.append(att.toString()).append("\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Convenience method for creating
	 * an email address expression (including
	 * the name, the address, or both).
	 * 
	 * @param mail The mail address.
	 * @param name The name part of the address.
	 * @return A combination of the name and address.
	 */
	public String createMailString(String mail, String name) {
		if ((mail == null) && (name == null)) {
			return null;
		}
		if (name == null) {
			return mail;
		}
		if (mail == null) {
			return name;
		}
		if (mail.equalsIgnoreCase(name)) {
			return mail;
		}
		return "\""+name+"\" <"+mail+">";
	}


	/**
	 * @return the attachments
	 */
	public List<Attachment> getAttachments() {
		return attachments;
	}


	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * @return the recipients
	 */
	public List<RecipientEntry> getRecipients() {
		return recipients;
	}

	/**
	 * @param recipients the recipients to set
	 */
	public void setRecipients(List<RecipientEntry> recipients) {
		this.recipients = recipients;
	}


	/**
	 * @return the fromEmail
	 */
	public String getFromEmail() {
		return fromEmail;
	}


	/**
	 * @param fromEmail the fromEmail to set
	 */
	public void setFromEmail(String fromEmail) {
		if(fromEmail != null && fromEmail.contains("@")) {
			setFromEmail(fromEmail, true);
		} else {
			setFromEmail(fromEmail, false);
		}
	}
	
	/**
	 * @param fromEmail the fromEmail to set
	 * @param force forces overwriting of the field if already set
	 */
	public void setFromEmail(String fromEmail, boolean force) {
		if ((force ||this.fromEmail == null) && fromEmail != null && fromEmail.contains("@")) {
			this.fromEmail = fromEmail;
		}
	}

	/**
	 * @return the fromName
	 */
	public String getFromName() {
		return fromName;
	}


	/**
	 * @param fromName the fromName to set
	 */
	public void setFromName(String fromName) {
		if (fromName != null) {
			this.fromName = fromName;
		}
	}

	public String getDisplayTo() {
		return displayTo;
	}

	public void setDisplayTo(String displayTo) {
		if (displayTo != null) {
			this.displayTo = displayTo;
		}
	}

	public String getDisplayCc() {
		return displayCc;
	}

	public void setDisplayCc(String displayCc) {
		if (displayCc != null) {
			this.displayCc = displayCc;
		}
	}

	public String getDisplayBcc() {
		return displayBcc;
	}

	public void setDisplayBcc(String displayBcc) {
		if (displayBcc != null) {
			this.displayBcc = displayBcc;
		}
	}

	/**
	 * @return the messageClass
	 */
	public String getMessageClass() {
		return messageClass;
	}


	/**
	 * @param messageClass the messageClass to set
	 */
	public void setMessageClass(String messageClass) {
		if (messageClass != null) {
			this.messageClass = messageClass;
		}
	}


	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}


	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(String messageId) {
		if (messageId != null) {
			this.messageId = messageId;
		}
	}


	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}


	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		if (subject != null) {
			this.subject = subject;
		}
	}


	/**
	 * @return the toEmail
	 */
	public String getToEmail() {
		return toEmail;
	}


	/**
	 * @param toEmail the toEmail to set
	 */
	public void setToEmail(String toEmail) {
		setToEmail(toEmail, false);
	}
	
	/**
	 * @param toEmail the toEmail to set
	 * @param force forces overwriting of the field if already set
	 */
	public void setToEmail(String toEmail, boolean force) {
		if ((force || this.toEmail == null) && toEmail != null && toEmail.contains("@")) {
			this.toEmail = toEmail;
		}
	}


	/**
	 * @return the toName
	 */
	public String getToName() {
		return toName;
	}


	/**
	 * @param toName the toName to set
	 */
	public void setToName(String toName) {
		if (toName != null) {
			toName = toName.trim();
			this.toName = toName;
		}
	}

	/**
	 * Retrieves the {@link RecipientEntry} object that represents the TO recipient of the message.
	 * 
	 * @return the TO recipient of the message or null in case no {@link RecipientEntry} was found.
	 */
	public RecipientEntry getToRecipient() {
		if(getDisplayTo() != null) {
			String recipientKey = getDisplayTo().trim();
			for (RecipientEntry entry : recipients) {
				String name = entry.getToName().trim();
				if (recipientKey.contains(name)) {
					return entry;
				} 
			}
		}
		return null;
	}
	/**
	 * Retrieves a list of {@link RecipientEntry} objects that represent the CC recipients of the message.
	 * 
	 * @return the CC recipients of the message.
	 */
	public List<RecipientEntry> getCcRecipients() {
		List<RecipientEntry> recipients = new ArrayList<>();
		String recipientKey = getDisplayCc().trim();
		for (RecipientEntry entry : recipients) {
			String name = entry.getToName().trim();
			if (recipientKey.contains(name)) {
				recipients.add(entry);
			} 
		}
		return recipients;
	}
	
	/**
	 * Retrieves a list of {@link RecipientEntry} objects that represent the BCC recipients of the message.
	 * 
	 * @return the BCC recipients of the message.
	 */
	public List<RecipientEntry> getBccRecipients() {
		List<RecipientEntry> recipients = new ArrayList<>();
		String recipientKey = getDisplayBcc().trim();
		for (RecipientEntry entry : recipients) {
			String name = entry.getToName().trim();
			if (recipientKey.contains(name)) {
				recipients.add(entry);
			} 
		}
		return recipients;
	}
	/**
	 * @return the bodyText
	 */
	public String getBodyText() {
		return bodyText;
	}

	/**
	 * @param bodyText the bodyText to set
	 */
	public void setBodyText(String bodyText) {
		if (this.bodyText == null && bodyText != null) {
			this.bodyText = bodyText;
		}
	}
	
	/**
	 * @return the bodyRTF
	 */
	public String getBodyRTF() {
		return bodyRTF;
	}


	/**
	 * @param bodyRTF the bodyRTF to set
	 */
	public void setBodyRTF(Object bodyRTF) {
		// we simply try to decompress the RTF data
		// if it's not compressed, the utils class 
		// is able to detect this anyway
		if(this.bodyRTF == null && bodyRTF != null) {
			if (bodyRTF instanceof byte[]) {
				byte[] decompressedBytes = decompressRtfBytes((byte[]) bodyRTF);
				if(decompressedBytes != null) {
					try {
						this.bodyRTF = new String(decompressedBytes, WINDOWS_CHARSET);
						setConvertedBodyHTML(rtf2htmlConverter.rtf2html(this.bodyRTF));
					} catch(Exception e) {
						logger.log(Level.WARNING, "Could not convert RTF body to HTML.", e);
					}
				}
 			} else {
				logger.log(Level.FINEST, "Unexpected data type "+bodyRTF.getClass());    			
			}
		}
	}
	/**
	 * @return the bodyHTML
	 */
	public String getBodyHTML() {
		return bodyHTML;
	}
	
	/**
	 * @param bodyHTML the bodyHTML to set
	 */
	public void setBodyHTML(String bodyHTML) {
		setBodyHTML(bodyHTML, false);
	}
	
	/**
	 * @return the convertedBodyHTML which is basically the result of an RTF-HTML conversion
	 */
	public String getConvertedBodyHTML() {
		return convertedBodyHTML;
	}
	
	/**
	 * @param convertedBodyHTML the bodyHTML to set
	 */
	public void setConvertedBodyHTML(String convertedBodyHTML) {
		this.convertedBodyHTML = convertedBodyHTML;
	}
	
	/**
	 * @param bodyToSet the bodyHTML to set
	 * @param force forces overwriting of the field if already set
	 */
	protected void setBodyHTML(String bodyToSet, boolean force) {
		if ((force || this.bodyHTML == null) && bodyToSet != null) {
			if(!(this.bodyHTML != null && this.bodyHTML.length() > bodyToSet.length())) {
				//only if the new body to be set is bigger than the current one
				//thus the short one is most probably wrong
				this.bodyHTML = bodyToSet;
			}
		}
	}
	/**
	 * @return the headers
	 */
	public String getHeaders() {
		return headers;
	}


	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(String headers) {
		if(headers != null) {
			this.headers = headers;
			// try to parse the date from the headers
			Date d = Message.getDateFromHeaders(headers);
			if (d != null) {
				this.setDate(d);
			}
			String s = Message.getFromEmailFromHeaders(headers);
			if (s != null) {
				this.setFromEmail(s);
			}
		}
	}
	
	/**
	 * Parses the sender's email address from the mail headers.
	 * @param headers The headers in a single String object
	 * @return The sender's email or null if nothing was found.
	 */
	protected static String getFromEmailFromHeaders(String headers) {
		String fromEmail = null;
		if (headers != null) {
			String[] headerLines = headers.split("\n");
			for (String headerLine : headerLines) {
				if(headerLine.toUpperCase().startsWith("FROM: ")) {
					String[] tokens = headerLine.split(" ");
					for(String t : tokens) {
						if(t.contains("@")) {
							fromEmail = t;
							fromEmail = fromEmail.replaceAll("[<>]", "");
							fromEmail = fromEmail.trim();
							break;
						}
					}
				}
				if(fromEmail != null) {
					break;
				}
			}
		}
		
		return fromEmail;
	}
	/**
	 * Parses the message date from the mail headers.
	 * 
	 * @param headers The headers in a single String object
	 * @return The Date object or null, if no valid Date:
	 *   has been found
	 */
	public static Date getDateFromHeaders(String headers) {
		if (headers == null) {
			return null;
		}
		String[] headerLines = headers.split("\n");
		for (String headerLine : headerLines) {
			if (headerLine.toLowerCase().startsWith("date:")) {
				String dateValue = headerLine.substring("Date:".length()).trim();
				SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

				// There may be multiple Date: headers. Let's take the first one that can be parsed. 

				try {
					Date date = formatter.parse(dateValue);

					if (date != null) {
						return date;
					}
				} catch(Exception e) {
					logger.log(Level.FINEST, "Could not parse date "+dateValue, e);
				}
			}
		}
		return null;
	}


	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}


	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getClientSubmitTime() {
		return clientSubmitTime;
	}

	public void setClientSubmitTime(String value) {
		if (value != null) {
			Date d = Message.parseDateString(value);
			if (d != null) {
				this.clientSubmitTime = d;
			}
		}
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String value) {
		if (value != null) {
			Date d = Message.parseDateString(value);
			if (d != null) {
				this.creationDate = d;
				setDate(d);
			}
		}
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(String value) {
		if (value != null) {
			Date d = Message.parseDateString(value);
			if (d != null) {
				this.lastModificationDate = d;
			}
		}
	}
	/**
	 * This method should no longer be used due to the fact that
	 * message properties are now stored with their keys being represented 
	 * as integers.
	 * @return All available keys properties have been found for.
	 */
	@Deprecated
	public Set<String> getProperties() {
		return getPropertiesAsHex();
	}
	
	/**
	 * This method provides a convenient way of retrieving
	 * property keys for all guys that like to stick to hex values.
	 * <br>Note that this method includes parsing of string values
	 * to integers which will be less efficient than using 
	 * {@link #getPropertyCodes()}.
	 * @return All available keys properties have been found for.
	 */
	public Set<String> getPropertiesAsHex() {
		Set<Integer> keySet = this.properties.keySet();
		Set<String> result = new HashSet<>();
		for(Integer k : keySet) {
			String s = convertToHex(k);
			result.add(s);
		}
		
		return result;
	}
	
	/**
	 * This method should no longer be used due to the fact that message properties are now stored with their keys being
	 * represented as integers. <br>
	 * <br>
	 * Please refer to {@link #getPropertyCodes()} for dealing with integer based keys.
	 * 
	 * @return The value for the requested property.
	 */
	@Deprecated
	public Object getProperty(String name) {
		return getPropertyFromHex(name);
	}

	/**
	 * This method provides a convenient way of retrieving properties for all guys that like to stick to hex values. <br>
	 * Note that this method includes parsing of string values to integers which will be less efficient than using
	 * {@link #getPropertyValue(Integer)}.
	 * 
	 * @param name
	 *            The hex notation of the property to be retrieved.
	 * @return The value for the requested property.
	 */
	public Object getPropertyFromHex(String name) {
		Integer i = -1;
		try {
			i = Integer.parseInt(name, 16);
		} catch (NumberFormatException e) {
			logger.log(Level.FINEST, "Could not parse integer: " + name);
		}
		return getPropertyValue(i);
	}

	/**
	 * This method returns a list of all available properties.
	 * 
	 * @return All available keys properties have been found for.
	 */
	public Set<Integer> getPropertyCodes() {
		return this.properties.keySet();
	}

	/**
	 * This method retrieves the value for a specific property.
	 * <p>
	 * <b>NOTE:</b> You can also use fields defined within {@link MAPIProperty} to easily read certain properties.
	 * 
	 * @param code
	 *            The key for the property to be retrieved.
	 * @return The value of the specified property.
	 */
	public Object getPropertyValue(Integer code) {
		return this.properties.get(code);
	}

	/**
	 * Generates a string that can be used to debug the properties of the msg.
	 * 
	 * @return A property listing holding hexadecimal, decimal and string representations of properties and their
	 *         values.
	 */
	public String getPropertyListing() {
		StringBuilder sb = new StringBuilder();
		for (Integer propCode : getPropertyCodes()) {
			Object value = getPropertyValue(propCode);
			String hexCode = "0x" + convertToHex(propCode);
			sb.append(hexCode).append(" / ").append(propCode);
			sb.append(": ").append(value.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Converts a given integer to hex notation without leading '0x'.
	 * @param propCode The value to be formatted.
	 * @return A hex formatted number.
	 */
	public String convertToHex(Integer propCode) {
		return String.format("%04x", propCode);
	}
}
