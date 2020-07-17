package org.simplejavamail.outlookmessageparser.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.poi.hmef.CompressedRTF;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.bbottema.rtftohtml.RTF2HTMLConverter;
import org.bbottema.rtftohtml.impl.RTF2HTMLConverterRFCCompliant;
import org.bbottema.rtftohtml.impl.util.CharsetHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.String.format;
import static java.util.Arrays.copyOfRange;
import static java.util.regex.Pattern.compile;

/**
 * Class that represents a .msg file. Some fields from the .msg file are stored in special parameters (e.g., {@link #fromEmail}). Attachments are stored in the
 * property {@link #outlookAttachments}). An attachment may be of the type {@link OutlookMsgAttachment} which represents another attached (encapsulated) .msg
 * object.
 */
public class OutlookMessage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlookMessage.class);
	
	/**
	 * The message class as defined in the .msg file.
	 */
	private String messageClass = "IPM.Note";
	/**
	 * The message Id.
	 */
	private String messageId;
	/**
	 * The address part of From: mail address.
	 */
	private String fromEmail;
	/**
	 * The name part of the From: mail address
	 */
	private String fromName;
	/**
	 * The address part of Reply-To header
	 */
	private String replyToEmail;
	/**
	 * The name part of Reply-To header
	 */
	private String replyToName;
	/**
	 * The S/MIME part of the S/MIME header
	 */
	private OutlookSmime smime;
	/**
	 * The mail's subject.
	 */
	private String subject;
	/**
	 * The normalized body text.
	 */
	private String bodyText;
	/**
	 * The displayed To: field
	 */
	private String displayTo;
	/**
	 * The displayed Cc: field
	 */
	private String displayCc;
	/**
	 * The displayed Bcc: field
	 */
	private String displayBcc;

	/**
	 * The body in RTF format (if available)
	 */
	private String bodyRTF;

	/**
	 * The body in HTML format (if available)
	 */
	private String bodyHTML;

	/**
	 * The body in HTML format (converted from RTF)
	 */
	private String convertedBodyHTML;
	/**
	 * Email headers (if available)
	 */
	private String headers;

	/**
	 * Email Date
	 */
	private Date date;

	/**
	 * Client Submit Time
	 */
	private Date clientSubmitTime;

	private Date creationDate;

	private Date lastModificationDate;
	/**
	 * A list of all outlookAttachments (both {@link OutlookFileAttachment}
	 * and {@link OutlookMsgAttachment}).
	 */
	private final List<OutlookAttachment> outlookAttachments = new ArrayList<>();
	/**
	 * Contains all properties that are not
	 * covered by the special properties.
	 */
	private final Map<Integer, Object> properties = new TreeMap<>();
	/**
	 * A list containing all recipients for this message
	 * (which can be set in the 'to:', 'cc:' and 'bcc:' field, respectively).
	 */
	private final List<OutlookRecipient> recipients = new ArrayList<>();

	private final RTF2HTMLConverter rtf2htmlConverter;
	
	public OutlookMessage() {
		rtf2htmlConverter = RTF2HTMLConverterRFCCompliant.INSTANCE;
	}

	public OutlookMessage(final RTF2HTMLConverter rtf2htmlConverter) {
		this.rtf2htmlConverter = (rtf2htmlConverter != null) ? rtf2htmlConverter : RTF2HTMLConverterRFCCompliant.INSTANCE;
	}

	public void addAttachment(final OutlookAttachment outlookAttachment) {
		outlookAttachments.add(outlookAttachment);
	}

	public void addRecipient(final OutlookRecipient recipient) {
		recipients.add(recipient);
	}

	/**
	 * Sets the name/value pair in the {@link #properties} map. Some properties are put into special attributes (e.g., {@link #setSubject(String)} when the property name is '0x37').
	 */
	@SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
	public void setProperty(final OutlookMessageProperty msgProp) {
		final String name = msgProp.getClazz();
		final Object value = msgProp.getData();

		if (name == null || value == null) {
			return;
		}

		//Most fields expect a String representation of the value
		final String stringValue = convertValueToString(value);

		int mapiClass = -1;
		try {
			mapiClass = Integer.parseInt(name, 16);
		} catch (final NumberFormatException e) {
			LOGGER.trace("Unexpected type: {}", name, e);
		}

		switch (mapiClass) {
			case 0x1a: //MESSAGE CLASS
				setMessageClass(stringValue);
				break;
			case 0x1035:
				setMessageId(stringValue);
				break;
			case 0x37: //SUBJECT
			case 0xe1d: //NORMALIZED SUBJECT
				setSubject(stringValue);
				break;
			case 0xc1f: //SENDER EMAIL ADDRESS
			case 0x65: //SENT REPRESENTING EMAIL ADDRESS
			case 0x3ffa: //LAST MODIFIER NAME
				setFromEmail(stringValue);
				break;
			case 0x42: //SENT REPRESENTING NAME
				setFromName(stringValue);
				break;
			case 0xe04: //DISPLAY TO
				setDisplayTo(stringValue);
				break;
			case 0xe03: //DISPLAY CC
				setDisplayCc(stringValue);
				break;
			case 0xe02: //DISPLAY BCC
				setDisplayBcc(stringValue);
				break;
			case 0x1013: //HTML
				setBodyHTML(stringValue);
				break;
			case 0x1000: //BODY
				setBodyText(stringValue);
				break;
			case 0x1009: //RTF COMPRESSED
				setBodyRTF(value);
				break;
			case 0x7d: //TRANSPORT MESSAGE HEADERS
				setHeaders(stringValue);
				break;
			case 0x3007: //CREATION TIME
				setCreationDate(stringValue);
				break;
			case 0x3008: //LAST MODIFICATION TIME
				setLastModificationDate(stringValue);
				break;
			case 0x39: //CLIENT SUBMIT TIME
				setClientSubmitTime(stringValue);
				break;
			case  0x8003: // S/MIME details
				setSmimeMultipartSigned(stringValue);
				break;
			case  0x8005: // S/MIME details
				setSmimeApplicationSmime(stringValue);
				break;
		}

		// save all properties (incl. those identified above)
		properties.put(mapiClass, value);

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

	private String convertValueToString(final Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof byte[]) {
			return new String((byte[]) value, StandardCharsets.UTF_8);
		} else {
			LOGGER.trace("Unexpected body class: {} (expected String or byte[])", value.getClass().getName());
			return value.toString();
		}
	}
	
	/**
	 * @return Only the attachments that are embedded by cid reference.
	 */
	public Map<String, OutlookFileAttachment> fetchCIDMap() {
		final HashMap<String, OutlookFileAttachment> cidMap = new HashMap<>();
		final String html = getConvertedBodyHTML();

		if (html != null && html.length() != 0) {
			for (final OutlookAttachment attachment : getOutlookAttachments()) {
				if (attachment instanceof OutlookFileAttachment) {
					final OutlookFileAttachment fileAttachment = (OutlookFileAttachment) attachment;
					if (!tryAddCid(cidMap, html, fileAttachment, fileAttachment.getContentId())) {
						if (!tryAddCid(cidMap, html, fileAttachment, fileAttachment.getFilename())) {
							tryAddCid(cidMap, html, fileAttachment, fileAttachment.getLongFilename());
						}
					}
				}
			}
		}
		return cidMap;
	}
	
	private boolean tryAddCid(HashMap<String, OutlookFileAttachment> cidMap, String html, OutlookFileAttachment a, String cid) {
		final boolean cidFound = cid != null && cid.length() != 0 && htmlContainsCID(html, cid);
		if (cidFound) {
			cidMap.put(cid, a);
		}
		return cidFound;
	}
	
	/**
	 * @return Only the downloadable attachments, *not* embedded attachments (as in embedded with cid:attachment, such as images in an email). This includes
	 * downloadable nested outlook messages as file attachments!
	 */
	public List<OutlookFileAttachment> fetchTrueAttachments() {
		final Set<OutlookAttachment> allAttachments = new HashSet<>(getOutlookAttachments());
		allAttachments.removeAll(fetchCIDMap().values());
		final ArrayList<OutlookFileAttachment> fileAttachments = new ArrayList<>();
		for (final OutlookAttachment attachment : allAttachments) {
			if (attachment instanceof OutlookFileAttachment) {
				fileAttachments.add((OutlookFileAttachment) attachment);
			} else {
				LOGGER.warn("Skipping nested Outlook message as file attachment, writing Outlook messages back as data is not supported!");
				LOGGER.warn("To access the nested Outlook message as parsed Java object, refer to .getAttachments() instead.");
			}
		}
		return fileAttachments;
	}
	
	private boolean htmlContainsCID(final String html, final String cidName) {
		return compile(format("cid:['\"]?%s['\"]?", escapeCID(cidName))).matcher(html).find();
	}

	private String escapeCID(final String cidName) {
		String res = cidName;
		for (final String c : new String[]{"\\", "^", "$", ".", "|", "?", "*", "+", "(", ")", "[", "{"}) {
			res = res.replace(c, "\\" + c);
		}
		return res;
	}

	/**
	 * @param date The date string to be converted (e.g.: 'Mon Jul 23 15:43:12 CEST 2012')
	 * @return A {@link Date} object representing the given date string.
	 */
	private static Date parseDateString(final String date) {
		//in order to parse the date we try using the US locale before we 
		//fall back to the default locale.
		final List<SimpleDateFormat> sdfList = new ArrayList<>(2);
		sdfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US));
		sdfList.add(new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"));

		Date d = null;
		for (final SimpleDateFormat sdf : sdfList) {
			try {
				d = sdf.parse(date);
				if (d != null) {
					break;
				}
			} catch (final ParseException e) {
				LOGGER.trace("Unexpected date format for date {}", date, e);
			}
		}
		return d;
	}

	/**
	 * Decompresses compressed RTF data.
	 *
	 * @param value Data to be decompressed.
	 * @return A byte array representing the decompressed data.
	 */
	private byte[] decompressRtfBytes(final byte[] value) {
		byte[] decompressed = null;
		if (value != null) {
			try {
				final CompressedRTF crtf = new CompressedRTF();
				decompressed = crtf.decompress(new ByteArrayInputStream(value));
			} catch (final IOException e) {
				LOGGER.info("Could not decompress RTF data", e);
			}
		}
		return decompressed;
	}

	@Override
	public String toString() {
		final StringBuilder sb = commonToString();
		sb.append(outlookAttachments.size()).append(" outlookAttachments.");
		return sb.toString();
	}
	
	private StringBuilder commonToString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("From: ").append(createMailString(fromEmail, fromName)).append("\n");
		appendRecipients(sb, "To: ", getToRecipients());
		appendRecipients(sb, "Cc: ", getCcRecipients());
		appendRecipients(sb, "Bcc: ", getBccRecipients());
		if (date != null) {
			final SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			sb.append("Date: ").append(formatter.format(date)).append("\n");
		}
		if (subject != null) {
			sb.append("Subject: ").append(subject).append("\n");
		}
		return sb;
	}
	
	private void appendRecipients(StringBuilder sb, String recipientTypeFormatted, List<OutlookRecipient> recipients) {
		if (!recipients.isEmpty()) {
			sb.append(recipientTypeFormatted);
			for (OutlookRecipient toRecipient : getToRecipients()) {
				sb.append(createMailString(toRecipient.getName(), toRecipient.getAddress())).append("; ");
			}
			sb.delete(sb.lastIndexOf("; "), sb.length());
		}
	}
	
	/**
	 * Convenience method for creating an email address expression (including the name, the address, or both).
	 *
	 * @param mail The mail address.
	 * @param name The name part of the address.
	 * @return A combination of the name and address.
	 */
	private String createMailString(final String mail, final String name) {
		if (mail == null && name == null) {
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
		return "\"" + name + "\" <" + mail + ">";
	}
	
	void setSmimeApplicationSmime(String smimeHeader) {
		// application/pkcs7-mime;smime-type=signed-data;name=smime.p7m
		if (smimeHeader != null && smimeHeader.contains("application/")) {
			final String[] smimeHeaderParts = smimeHeader.split(";");
			String smimeMime = smimeHeaderParts[0].trim();
			String smimeType = null;
			String smimeName = null;
			for (String smimeHeaderParam : copyOfRange(smimeHeaderParts, 1, smimeHeaderParts.length)) {
				final String[] smimeParamParts = smimeHeaderParam.split("=");
				String paramName = smimeParamParts[0].trim();
				String paramValue = smimeParamParts[1].trim();
				if (paramName.equals("smime-type")) {
					smimeType = paramValue;
				} else if (paramName.equals("name")) {
					smimeName = paramValue;
				}
			}
			setSmime(new OutlookSmime.OutlookSmimeApplicationSmime(smimeMime, smimeType, smimeName));
		}
	}
	
	void setSmimeMultipartSigned(String smimeHeader) {
		// multipart/signed;protocol="application/pkcs7-signature";micalg=sha1
		if (smimeHeader != null && smimeHeader.contains("multipart/signed")) {
			final String[] smimeHeaderParts = smimeHeader.split(";");
			String smimeMime = smimeHeaderParts[0].trim();
			String smimeProtocol = null;
			String smimeMicalg = null;
			for (String smimeHeaderParam : copyOfRange(smimeHeaderParts, 1, smimeHeaderParts.length)) {
				final String[] smimeParamParts = smimeHeaderParam.split("=");
				final String paramName = smimeParamParts[0].trim();
				final String paramValue = smimeParamParts[1].trim().replaceFirst("^\"(.*)\"$", "$1");
				
				if (paramName.equals("protocol")) {
					smimeProtocol = paramValue;
				} else if (paramName.equals("micalg")) {
					smimeMicalg = paramValue;
				}
			}
			setSmime(new OutlookSmime.OutlookSmimeMultipartSigned(smimeMime, smimeProtocol, smimeMicalg));
		}
	}

	/**
	 * Bean getter for {@link #outlookAttachments}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public List<OutlookAttachment> getOutlookAttachments() {
		return outlookAttachments;
	}

	/**
	 * Bean getter for {@link #recipients}.
	 */
	public List<OutlookRecipient> getRecipients() {
		return recipients;
	}

	/**
	 * Bean getter for {@link #fromEmail}.
	 */
	public String getFromEmail() {
		return fromEmail;
	}

	/**
	 * Bean setter for {@link #fromEmail}. Uses force if the email contains a '@' symbol ({@link #setFromEmail(String, boolean)}).
	 */
	private void setFromEmail(final String fromEmail) {
		setFromEmail(fromEmail, fromEmail != null && fromEmail.contains("@"));
	}

	/**
	 * @param fromEmail the fromEmail to set
	 * @param force     forces overwriting of the field if already set
	 */
	private void setFromEmail(final String fromEmail, final boolean force) {
		if ((force || this.fromEmail == null) && fromEmail != null && fromEmail.contains("@")) {
			this.fromEmail = fromEmail;
		}
	}

	/**
	 * Bean getter for {@link #fromName}.
	 */
	public String getFromName() {
		return fromName;
	}

	/**
	 * Bean setter for {@link #fromName}.
	 */
	private void setFromName(final String fromName) {
		if (fromName != null) {
			this.fromName = fromName;
		}
	}

	/**
	 * Bean getter for {@link #displayTo}.
	 */
	public String getDisplayTo() {
		return displayTo;
	}

	/**
	 * Bean setter for {@link #displayTo}.
	 */
	private void setDisplayTo(final String displayTo) {
		if (displayTo != null) {
			this.displayTo = displayTo;
		}
	}

	/**
	 * Bean getter for {@link #displayCc}.
	 */
	public String getDisplayCc() {
		return displayCc;
	}

	/**
	 * Bean setter for {@link #displayCc}.
	 */
	private void setDisplayCc(final String displayCc) {
		if (displayCc != null) {
			this.displayCc = displayCc;
		}
	}

	/**
	 * Bean getter for {@link #displayBcc}.
	 */
	public String getDisplayBcc() {
		return displayBcc;
	}

	/**
	 * Bean setter for {@link #displayBcc}.
	 */
	private void setDisplayBcc(final String displayBcc) {
		if (displayBcc != null) {
			this.displayBcc = displayBcc;
		}
	}

	/**
	 * Bean getter for {@link #messageClass}.
	 */
	public String getMessageClass() {
		return messageClass;
	}

	/**
	 * Bean setter for {@link #messageClass}.
	 */
	private void setMessageClass(final String messageClass) {
		if (messageClass != null) {
			this.messageClass = messageClass;
		}
	}

	/**
	 * Bean getter for {@link #messageId}.
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Bean setter for {@link #messageId}.
	 */
	private void setMessageId(final String messageId) {
		if (messageId != null) {
			this.messageId = messageId;
		}
	}

	/**
	 * Bean getter for {@link #subject}.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Bean setter for {@link #subject}.
	 */
	private void setSubject(final String subject) {
		if (subject != null) {
			this.subject = subject;
		}
	}

	/**
	 * Retrieves a list of {@link OutlookRecipient} objects that represent the CC recipients of the message.
	 *
	 * @return the TO recipients of the message.
	 */
	public List<OutlookRecipient> getToRecipients() {
		return filterRecipients(getDisplayTo());
	}
	
	/**
	 * Retrieves a list of {@link OutlookRecipient} objects that represent the CC recipients of the message.
	 *
	 * @return the CC recipients of the message.
	 */
	public List<OutlookRecipient> getCcRecipients() {
		return filterRecipients(getDisplayCc());
	}

	/**
	 * Retrieves a list of {@link OutlookRecipient} objects that represent the BCC recipients of the message.
	 *
	 * @return the BCC recipients of the message.
	 */
	public List<OutlookRecipient> getBccRecipients() {
		return filterRecipients(getDisplayBcc());
	}
	
	@NotNull
	private List<OutlookRecipient> filterRecipients(String displayTo) {
		final List<OutlookRecipient> toRecipients = new ArrayList<>();
		final String recipientKey = displayTo.trim();
		for (final OutlookRecipient entry : recipients) {
			final String name = entry.getName().trim();
			if (recipientKey.contains(name)) {
				toRecipients.add(entry);
			}
		}
		return toRecipients;
	}

	/**
	 * Bean getter for {@link #bodyText}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getBodyText() {
		return bodyText;
	}

	/**
	 * Bean setter for {@link #bodyText}.
	 */
	private void setBodyText(final String bodyText) {
		if (this.bodyText == null && bodyText != null) {
			this.bodyText = bodyText;
		}
	}

	/**
	 * Bean getter for {@link #bodyRTF}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getBodyRTF() {
		return bodyRTF;
	}

	/**
	 * @param bodyRTF the bodyRTF to set
	 */
	private void setBodyRTF(final Object bodyRTF) {
		// we simply try to decompress the RTF data if it's not compressed, the utils class is able to detect this anyway
		if (this.bodyRTF == null && bodyRTF != null) {
			if (bodyRTF instanceof byte[]) {
				try {
					final byte[] decompressedBytes = decompressRtfBytes((byte[]) bodyRTF);
					if (decompressedBytes != null) {
						this.bodyRTF = new String(decompressedBytes, CharsetHelper.WINDOWS_CHARSET);
						setConvertedBodyHTML(rtf2htmlConverter.rtf2html(this.bodyRTF));
					}
				} catch (IllegalArgumentException e) {
					LOGGER.info("Error occurred while extracting compressed RTF from source msg", e);
				}
			} else {
				LOGGER.warn("Unexpected data type {}", bodyRTF.getClass());
			}
		}
	}

	/**
	 * Bean getter for {@link #bodyHTML}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getBodyHTML() {
		return bodyHTML;
	}

	/**
	 * Bean getter for {@link #convertedBodyHTML}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getConvertedBodyHTML() {
		return convertedBodyHTML;
	}

	/**
	 * Bean setter for {@link #convertedBodyHTML}.
	 */
	private void setConvertedBodyHTML(final String convertedBodyHTML) {
		this.convertedBodyHTML = convertedBodyHTML;
	}

	/**
	 * Sets {@link #bodyHTML} if empty or if given bodySet is longer
	 */
	private void setBodyHTML(final String bodyToSet) {
		if (bodyToSet != null && (bodyHTML == null || bodyHTML.length() <= bodyToSet.length())) {
			//only if the new body to be set is bigger than the current one
			//thus the short one is most probably wrong
			bodyHTML = bodyToSet;
		}
	}

	/**
	 * Bean getter for {@link #headers}.
	 */
	public String getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	private void setHeaders(final String headers) {
		if (headers != null) {
			this.headers = headers;
			// try to parse the date from the headers
			final Date d = getDateFromHeaders(headers);
			if (d != null) {
				setDate(d);
			}
			final String s = getFromEmailFromHeaders(headers);
			if (s != null) {
				setFromEmail(s);
			}
		}
	}

	/**
	 * Parses the sender's email address from the mail headers.
	 *
	 * @param headers The headers in a single String object
	 * @return The sender's email or null if nothing was found.
	 */
	private static String getFromEmailFromHeaders(final String headers) {
		if (headers != null) {
			final String[] headerLines = headers.split("\n");
			for (final String headerLine : headerLines) {
				if (headerLine.toUpperCase().startsWith("FROM: ")) {
					final String[] tokens = headerLine.split(" ");
					for (final String potentialFromEmailToken : tokens) {
						if (potentialFromEmailToken.contains("@")) {
							return potentialFromEmailToken.replaceAll("[<>]", "").trim();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Parses the message date from the mail headers.
	 *
	 * @param headers The headers in a single String object
	 * @return The Date object or null, if no valid Date: has been found
	 */
	private static Date getDateFromHeaders(final String headers) {
		if (headers != null) {
			final String[] headerLines = headers.split("\n");
			for (final String headerLine : headerLines) {
				if (headerLine.toLowerCase().startsWith("date:")) {
					final String dateValue = headerLine.substring("Date:".length()).trim();
					final SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);

					// There may be multiple Date: headers. Let's take the first one that can be parsed.
					try {
						final Date date = formatter.parse(dateValue);
						if (date != null) {
							return date;
						}
					} catch (final ParseException e) {
						LOGGER.debug("Could not parse date {}, moving on to the next date candidate", dateValue, e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Bean getter for {@link #date}.
	 */
	public Date getDate() {
		return (Date) date.clone();
	}

	/**
	 * Bean setter for {@link #date}.
	 */
	private void setDate(final Date date) {
		this.date = date;
	}

	/**
	 * @return {@link #clientSubmitTime} or null in case this message has not been sent yet.
	 */
	@Nullable
	public Date getClientSubmitTime() {
		return clientSubmitTime != null ? (Date) clientSubmitTime.clone() : null;
	}

	private void setClientSubmitTime(final String value) {
		if (value != null) {
			final Date d = parseDateString(value);
			if (d != null) {
				clientSubmitTime = d;
			}
		}
	}

	/**
	 * Bean getter for {@link #creationDate}.
	 */
	public Date getCreationDate() {
		return (Date) creationDate.clone();
	}

	private void setCreationDate(final String value) {
		if (value != null) {
			final Date d = parseDateString(value);
			if (d != null) {
				creationDate = d;
				setDate(d);
			}
		}
	}

	/**
	 * Bean getter for {@link #lastModificationDate}.
	 */
	public Date getLastModificationDate() {
		return (Date) lastModificationDate.clone();
	}

	private void setLastModificationDate(final String value) {
		if (value != null) {
			final Date d = parseDateString(value);
			if (d != null) {
				lastModificationDate = d;
			}
		}
	}

	/**
	 * This method provides a convenient way of retrieving
	 * property keys for all guys that like to stick to hex values.
	 * <br>Note that this method includes parsing of string values
	 * to integers which will be less efficient than using
	 * {@link #getPropertyCodes()}.
	 *
	 * @return All available keys properties have been found for.
	 */
	public Set<String> getPropertiesAsHex() {
		final Set<Integer> keySet = properties.keySet();
		final Set<String> result = new HashSet<>();
		for (final Integer k : keySet) {
			final String s = convertToHex(k);
			result.add(s);
		}

		return result;
	}

	/**
	 * This method returns a list of all available properties.
	 *
	 * @return All available keys properties have been found for.
	 */
	public Set<Integer> getPropertyCodes() {
		return properties.keySet();
	}

	/**
	 * This method retrieves the value for a specific property.
	 * <p>
	 * <b>NOTE:</b> You can also use fields defined within {@link MAPIProperty} to easily read certain properties.
	 *
	 * @param code The key for the property to be retrieved.
	 * @return The value of the specified property.
	 */
	private Object getPropertyValue(final Integer code) {
		return properties.get(code);
	}

	/**
	 * Generates a string that can be used to debug the properties of the msg.
	 *
	 * @return A property listing holding hexadecimal, decimal and string representations of properties and their values.
	 */
	public String getPropertyListing() {
		final StringBuilder sb = new StringBuilder();
		for (final Integer propCode : getPropertyCodes()) {
			final Object value = getPropertyValue(propCode);
			final String hexCode = "0x" + convertToHex(propCode);
			sb.append(hexCode).append(" / ").append(propCode);
			sb.append(": ").append(value);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Converts a given integer to hex notation without leading '0x'.
	 *
	 * @param propCode The value to be formatted.
	 * @return A hex formatted number.
	 */
	private String convertToHex(final Integer propCode) {
		return format("%04x", propCode);
	}

	/**
	 * Bean getter for {@link #replyToEmail}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getReplyToEmail() {
		return replyToEmail;
	}

	/**
	 * Bean setter for {@link #replyToEmail}.
	 */
	public void setReplyToEmail(final String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}

	/**
	 * Bean getter for {@link #replyToName}.
	 */
	@SuppressWarnings("ElementOnlyUsedFromTestCode")
	public String getReplyToName() {
		return replyToName;
	}
	
	/**
	 * Bean setter for {@link #replyToName}.
	 */
	public void setReplyToName(final String replyToName) {
		this.replyToName = replyToName;
	}
	
	/**
	 * Bean setter for {@link #smime}.
	 */
	public void setSmime(OutlookSmime smime) {
		this.smime = smime;
	}
	
	/**
	 * Bean getter for {@link #smime}.
	 */
	public OutlookSmime getSmime() {
		return smime;
	}
}