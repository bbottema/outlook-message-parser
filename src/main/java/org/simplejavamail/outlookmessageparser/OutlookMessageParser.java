package org.simplejavamail.outlookmessageparser;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFieldInformation;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.simplejavamail.outlookmessageparser.model.OutlookMessageProperty;
import org.simplejavamail.outlookmessageparser.model.OutlookMsgAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookRecipient;
import org.simplejavamail.outlookmessageparser.rtf.RTF2HTMLConverter;
import org.simplejavamail.outlookmessageparser.rtf.SimpleRTF2HTMLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Main parser class that does the actual parsing of the Outlook .msg file. It uses the <a href="http://poi.apache.org/poifs/">POI</a> library for parsing the
 * .msg container file and is based on a description posted by Peter Fiskerstrand at <a href="http://www.fileformat.info/format/outlookmsg/">fileformat.info</a>.
 * <p>
 * It parses the .msg file and stores the information in a {@link OutlookMessage} object. Attachments are put into an {@link OutlookFileAttachment} object.
 * Hence, please keep in mind that the complete mail is held in the memory! If an attachment is another .msg file, this attachment is not processed as a normal
 * attachment but rather included as a {@link OutlookMsgAttachment}. This attached mail is, again, a {@link OutlookMessage} object and may have further
 * outlookAttachments and so on.
 * <p>
 * Furthermore there is a feature which allows us to extract HTML bodies when only RTF bodies are available. In order to achieve this a conversion class
 * implementing {@link RTF2HTMLConverter} is used. This can be overridden with a custom implementation as well (see code below for an example).
 * <p>
 * Note: this code has not been tested on a wide range of .msg files. Use in production level (as in any other level) at your own risk.
 * <p>
 * Usage:
 * <p>
 * <code> OutlookMessageParser msgp = new OutlookMessageParser();<br /> msgp.setRtf2htmlConverter(new SimpleRTF2HTMLConverter()); //optional (if you want to use
 * your own implementation)<br /> OutlookMessage msg = msgp.parseMsg("test.msg"); </code>
 */
public class OutlookMessageParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(OutlookMessageParser.class);

	private static final String PROPS_KEY = "__properties_version1.0";

	private static final String PROPERTY_STREAM_PREFIX = "__substg1.0_";

	private RTF2HTMLConverter rtf2htmlConverter = new SimpleRTF2HTMLConverter();

	/**
	 * Parses a .msg file provided in the specified file.
	 *
	 * @param msgFile The .msg file.
	 * @return A {@link OutlookMessage} object representing the .msg file.
	 * @throws IOException Thrown if the file could not be loaded or parsed.
	 */
	public OutlookMessage parseMsg(@Nonnull File msgFile)
			throws IOException {
		return parseMsg(new FileInputStream(msgFile));
	}

	/**
	 * Parses a .msg file provided in the specified file.
	 *
	 * @param msgFile The .msg file as a String path.
	 * @return A {@link OutlookMessage} object representing the .msg file.
	 * @throws IOException Thrown if the file could not be loaded or parsed.
	 */
	public OutlookMessage parseMsg(@Nonnull String msgFile)
			throws IOException {
		return parseMsg(new FileInputStream(msgFile));
	}

	/**
	 * Parses a .msg file provided by an input stream.
	 *
	 * @param msgFileStream The .msg file as a InputStream.
	 * @return A {@link OutlookMessage} object representing the .msg file.
	 * @throws IOException Thrown if the file could not be loaded or parsed.
	 */
	public OutlookMessage parseMsg(@Nonnull InputStream msgFileStream)
			throws IOException {
		// the .msg file, like a file system, contains directories and documents within this directories
		// we now gain access to the root node and recursively go through the complete 'filesystem'.
		final OutlookMessage msg = new OutlookMessage(rtf2htmlConverter);
		try {
			checkDirectoryEntry(new POIFSFileSystem(msgFileStream).getRoot(), msg);
		} finally {
			msgFileStream.close();
		}
		convertHeaders(msg);
		return msg;
	}

	private void convertHeaders(@Nonnull OutlookMessage msg) {
		String allHeaders = msg.getHeaders();
		if (allHeaders != null) {
			extractReplyToHeader(msg, allHeaders);
		}
	}

	static void extractReplyToHeader(@Nonnull OutlookMessage msg, @Nonnull String allHeaders) {
		// Reply-To: Optional Name <adress@somemail.com> // second '<' and '>' kept optional
		Matcher m = compile("^Reply-To:\\s*(?:<?(?<nameOrAddress>.*?)>?)?\\s*(?:<(?<address>.*?)>)?$", Pattern.MULTILINE).matcher(allHeaders);
		if (m.find()) {
			if (m.group("address") != null) {
				// found both name and email part
				msg.setReplyToName(m.group("nameOrAddress"));
				msg.setReplyToEmail(m.group("address"));
			} else if (m.group("nameOrAddress") != null) {
				// assume we found an email
				msg.setReplyToName(m.group("nameOrAddress"));
				msg.setReplyToEmail(m.group("nameOrAddress"));
			} /* else {
				// unknown results, ignore Reply-To data
			} */
		}
	}

	/**
	 * Recursively parses the complete .msg file with the help of the POI library. The parsed information is put into the {@link OutlookMessage} object.
	 *
	 * @param dir The current node in the .msg file.
	 * @param msg The resulting {@link OutlookMessage} object.
	 * @throws IOException Thrown if the .msg file could not be parsed.
	 */
	private void checkDirectoryEntry(DirectoryEntry dir, OutlookMessage msg)
			throws IOException {
		// we iterate through all entries in the current directory
		for (Iterator<?> iter = dir.getEntries(); iter.hasNext(); ) {
			Entry entry = (Entry) iter.next();

			// check whether the entry is either a directory entry or a document entry

			if (entry.isDirectoryEntry()) {
				DirectoryEntry de = (DirectoryEntry) entry;
				// outlookAttachments have a special name and have to be handled separately at this point
				if (de.getName().startsWith("__attach_version1.0")) {
					parseAttachment(de, msg);
				} else if (de.getName().startsWith("__recip_version1.0")) {
					// a recipient entry has been found (which is also a directory entry itself)
					checkRecipientDirectoryEntry(de, msg);
				} else {
					// a directory entry has been found. this node will be recursively checked
					checkDirectoryEntry(de, msg);
				}
			} else if (entry.isDocumentEntry()) {
				// a document entry contains information about the mail (e.g, from, to, subject, ...)
				DocumentEntry de = (DocumentEntry) entry;
				checkDirectoryDocumentEntry(de, msg);
			} /* else {
				// any other type is not supported
			} */
		}
	}

	/**
	 * Parses a recipient directory entry which holds informations about one of possibly multiple recipients.
	 * The parsed information is put into the {@link OutlookMessage} object.
	 *
	 * @param dir The current node in the .msg file.
	 * @param msg The resulting {@link OutlookMessage} object.
	 * @throws IOException Thrown if the .msg file could not be parsed.
	 */
	private void checkRecipientDirectoryEntry(DirectoryEntry dir, OutlookMessage msg)
			throws IOException {
		OutlookRecipient recipient = new OutlookRecipient();

		// we iterate through all entries in the current directory
		for (Iterator<?> iter = dir.getEntries(); iter.hasNext(); ) {
			Entry entry = (Entry) iter.next();

			// check whether the entry is either a directory entry
			// or a document entry, while we are just interested in document entries on this level			
			if (!entry.isDirectoryEntry() && entry.isDocumentEntry()) {
				// a document entry contains information about the mail (e.g, from, to, subject, ...)
				DocumentEntry de = (DocumentEntry) entry;
				checkRecipientDocumentEntry(de, recipient);
			}
		}

		//after all properties are set -> add recipient to msg object
		msg.addRecipient(recipient);
	}

	/**
	 * Parses a directory document entry which can either be a simple entry or
	 * a stream that has to be split up into multiple document entries again.
	 * The parsed information is put into the {@link OutlookMessage} object.
	 *
	 * @param de  The current node in the .msg file.
	 * @param msg The resulting {@link OutlookMessage} object.
	 * @throws IOException Thrown if the .msg file could not be parsed.
	 */
	private void checkDirectoryDocumentEntry(DocumentEntry de, OutlookMessage msg)
			throws IOException {
		if (de.getName().startsWith(PROPS_KEY)) {
			//TODO: parse properties stream
			List<DocumentEntry> deList = getDocumentEntriesFromPropertiesStream(de);
			for (DocumentEntry deFromProps : deList) {
				OutlookMessageProperty msgProp = getMessagePropertyFromDocumentEntry(deFromProps);
				msg.setProperty(msgProp);
			}
		} else {
			msg.setProperty(getMessagePropertyFromDocumentEntry(de));
		}
	}

	/**
	 * Parses a recipient document entry which can either be a simple entry or
	 * a stream that has to be split up into multiple document entries again.
	 * The parsed information is put into the {@link OutlookRecipient} object.
	 *
	 * @param de        The current node in the .msg file.
	 * @param recipient The resulting {@link OutlookRecipient} object.
	 * @throws IOException Thrown if the .msg file could not be parsed.
	 */
	private void checkRecipientDocumentEntry(DocumentEntry de, OutlookRecipient recipient)
			throws IOException {
		if (de.getName().startsWith(PROPS_KEY)) {
			//TODO: parse properties stream
			List<DocumentEntry> deList = getDocumentEntriesFromPropertiesStream(de);
			for (DocumentEntry deFromProps : deList) {
				OutlookMessageProperty msgProp = getMessagePropertyFromDocumentEntry(deFromProps);
				recipient.setProperty(msgProp);
			}
		} else {
			recipient.setProperty(getMessagePropertyFromDocumentEntry(de));
		}
	}

	/**
	 * Parses a document entry which has been detected to be a stream of document entries itself. This stream is identified by the key
	 * "__properties_version1.0".
	 *
	 * @param de The stream to be parsed.
	 * @return A list of document entries for further processing.
	 * @throws IOException Thrown if the properties stream could not be parsed.
	 */
	private List<DocumentEntry> getDocumentEntriesFromPropertiesStream(DocumentEntry de)
			throws IOException {
		List<DocumentEntry> result = new ArrayList<>();
		DocumentInputStream dstream = null;
		try {
			dstream = new DocumentInputStream(de);

			int headerLength = 4;
			int flagsLength = 4;
			byte[] bytes = new byte[headerLength];
			while ((dstream.read(bytes)) == headerLength) {
				StringBuilder header = new StringBuilder();
				for (int i = bytes.length - 1; i >= 0; i--) {
					header.append(bytesToHex(new byte[] { bytes[i] }));
				}

				//header ready for use
				String type = header.substring(4);
				String clazz = header.substring(0, 4);

				int typeNumber = -1;
				try {
					typeNumber = Integer.parseInt(type, 16);
				} catch (NumberFormatException e) {
					LOGGER.error("Unexpected type: {}", type, e);
				}

				if (!clazz.equals("0000")) { // what is this?
					//reading and ignoring flags
					bytes = new byte[flagsLength];
					//noinspection ResultOfMethodCallIgnored
					dstream.read(bytes);
					//System.out.println("flags: " + bytesToHex(bytes));

					// reading data
					if (typeNumber == 0x48 //CLSID
							|| typeNumber == 0x1e //STRING
							|| typeNumber == 0x1f //UNICODE STRING
							|| typeNumber == 0xd //OBJECT
							|| typeNumber == 0x102) { //BINARY
						//found datatype with variable length, thus the value is stored in a separate string
						//no data available inside the properties stream
						//reading and ignoring size
						bytes = null;
						//noinspection ResultOfMethodCallIgnored
						dstream.read(new byte[4]);
					} else if (typeNumber == 0x3 //INT
							|| typeNumber == 0x4 //FLOAT
							|| typeNumber == 0xa //ERROR
							|| typeNumber == 0xb //BOOLEAN
							|| typeNumber == 0x2) { //SHORT
						// 4 bytes
						bytes = new byte[4];
						//noinspection ResultOfMethodCallIgnored
						dstream.read(bytes);
						//noinspection ResultOfMethodCallIgnored
						dstream.read(bytes); //read and ignore padding
					} else if (typeNumber == 0x5 //DOUBLE
							|| typeNumber == 0x7 //APPTIME
							|| typeNumber == 0x6 //CURRENCY
							|| typeNumber == 0x14 //INT8BYTE
							|| typeNumber == 0x40) { //SYSTIME
						// 8 bytes
						bytes = new byte[8];
						//noinspection ResultOfMethodCallIgnored
						dstream.read(bytes);
					}
					//stream ready for use

					if (bytes != null) {
						//creating new document entry for later processing of all document entries
						POIFSFileSystem poifs = new POIFSFileSystem();
						result.add(poifs.createDocument(new ByteArrayInputStream(bytes), "__substg1.0_" + header));
					}
				}

				//start over with new byte[] for next header
				bytes = new byte[headerLength];
			}

			return result;

		} finally {
			if (dstream != null) {
				dstream.close();
			}
		}

	}

	/**
	 * Reads a property from a document entry and puts it's type and data to a {@link OutlookMessageProperty} object.
	 *
	 * @param de The {@link DocumentEntry} to be read.
	 * @return An object holding the type and data of the read property.
	 * @throws IOException In case the property could not be parsed.
	 */
	private OutlookMessageProperty getMessagePropertyFromDocumentEntry(DocumentEntry de)
			throws IOException {
		// analyze the document entry
		// (i.e., get class and data type)
		OutlookFieldInformation info = analyzeDocumentEntry(de);
		// create a Java object from the data provided
		// by the input stream. depending on the field
		// information, either a String or a byte[] will
		// be returned. other datatypes are not yet supported
		Object data = getData(de, info);
		LOGGER.trace("  Document data: {}", data);
		return new OutlookMessageProperty(info.getClazz(), data, de.getSize());
	}

	/**
	 * Reads the information from the InputStream and
	 * creates, based on the information in the
	 * {@link OutlookFieldInformation} object, either a String
	 * or a byte[] (e.g., for outlookAttachments) Object
	 * containing this data.
	 *
	 * @param de   The Document Entry.
	 * @param info The field information that is needed to determine the data type of the input stream.
	 * @return The String/byte[] object representing the data.
	 * @throws IOException                   Thrown if the .msg file could not be parsed.
	 * @throws UnsupportedOperationException Thrown if the .msg file contains unknown data.
	 */
	private Object getData(DocumentEntry de, OutlookFieldInformation info)
			throws IOException {
		// if there is no field information available, we simply
		// return null. in that case, we're not interested in the
		// data anyway
		if (info == null) {
			return null;
		}

		// if the type is 001e (we know it is lower case
		// because analyzeDocumentEntry stores the type in
		// lower case), we create a String object from the data.
		// the encoding of the binary data is most probably
		// ISO-8859-1 (not pure ASCII).
		int mapiType = info.getMapiType();

		switch (mapiType) {
			case OutlookFieldInformation.UNKNOWN_MAPITYPE:
				// if there is no field information available, we simply return null
				// in that case, we're not interested in the data anyway
				return null;
			case 0x1e:
				// we put the complete data into a byte[] object...
				byte[] textBytes1e = getBytesFromDocumentEntry(de);
				// ...and create a String object from it
				return new String(textBytes1e, "ISO-8859-1");
			case 0x1f:
				// Unicode encoding with lowbyte followed by hibyte
				// Note: this is arcane guesswork, but it works
				byte[] textBytes1f = getBytesFromDocumentEntry(de);
				// now that we have all bytes from the stream,
				// we can now convert the byte array into
				// a character array by switching hi- and lowbytes
				char[] characters = new char[textBytes1f.length / 2];
				int c = 0;
				for (int i = 0; i < textBytes1f.length - 1; i = i + 2) {
					int ch = (int) textBytes1f[i + 1];
					int cl = (int) textBytes1f[i] & 0xff; //Using unsigned value (thanks to Reto Schuettel)
					characters[c++] = (char) ((ch << 8) + cl);
				}
				return new String(characters);
			case 0x102:
				try {
					// the data is read into a byte[] object
					// and returned as-is
					return getBytesFromDocumentEntry(de);
				} catch (IOException e) {
					LOGGER.error("Could not get content of byte array of field 0x102", e);
					// To keep compatible with previous implementations, we return an empty array here
					return new byte[0];
				}
			case 0x40:
				// The following part has been provided by Morten Sørensen (Thanks!)

				// This parsing has been lifted from the MsgViewer project
				// https://sourceforge.net/projects/msgviewer/

				// the data is read into a byte[] object
				byte[] bytes = getBytesFromDocumentEntry(de);
				// Read the byte array as little endian byteorder
				ByteBuffer buff = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
				buff.put(bytes);
				// Convert the bytes to a long
				// Nanoseconds since 1601
				Long timeLong = buff.getLong(0);
				// Convert to milliseconds
				timeLong /= 10000L;
				// Move the offset from since 1601 to 1970
				timeLong -= 11644473600000L;
				// Convert to a Date object, and return
				return new Date(timeLong);
			default:
				// this should not happen
				LOGGER.trace("Unknown field type {}", mapiType);
				return null;
		}

	}

	/**
	 * Reads the bytes from the DocumentEntry.  This is a convenience method that
	 * calls {@see #getBytesFromStream(InputStream)} internally. It ensures that the
	 * opened input stream is closed at the end.
	 *
	 * @param de The document entry that should be read.
	 * @return The bytes of the document entry.
	 * @throws IOException Thrown if the document entry could not be read.
	 */
	private byte[] getBytesFromDocumentEntry(DocumentEntry de)
			throws IOException {
		InputStream is = null;
		try {
			is = new DocumentInputStream(de);
			return getBytesFromStream(is);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOGGER.error("Could not close input stream for document entry", e);
				}
			}
		}
	}

	/**
	 * Reads the bytes from the stream to a byte array.
	 *
	 * @param dstream The stream to be read from.
	 * @return An array of bytes.
	 * @throws IOException If the stream cannot be read properly.
	 */
	private byte[] getBytesFromStream(InputStream dstream)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read;
		while ((read = dstream.read(buffer)) > 0) {
			baos.write(buffer, 0, read);
		}
		return baos.toByteArray();
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder byteStr = new StringBuilder();
		for (byte aByte : bytes) {
			byteStr.append(String.format("%02X", aByte & 0xff));
		}
		return byteStr.toString();
	}

	/**
	 * Analyzes the {@link DocumentEntry} and returns
	 * a {@link OutlookFieldInformation} object containing the
	 * class (the field name, so to say) and type of
	 * the entry.
	 *
	 * @param de The {@link DocumentEntry} that should be examined.
	 * @return A {@link OutlookFieldInformation} object containing class and type of the document entry or, if the entry is not an interesting field, an empty
	 * {@link OutlookFieldInformation} object containing {@link OutlookFieldInformation#UNKNOWN} class and type.
	 */
	private OutlookFieldInformation analyzeDocumentEntry(DocumentEntry de) {
		String name = de.getName();
		// we are only interested in document entries
		// with names starting with __substg1.
		LOGGER.trace("Document entry: {}", name);
		if (name.startsWith(PROPERTY_STREAM_PREFIX)) {
			String clazz;
			String type;
			int mapiType;
			try {
				String val = name.substring(PROPERTY_STREAM_PREFIX.length()).toLowerCase();
				// the first 4 digits of the remainder
				// defines the field class (or field name)
				// and the last 4 digits indicate the
				// data type.
				clazz = val.substring(0, 4);
				type = val.substring(4);
				LOGGER.trace("  Found document entry: class={}, type={}", clazz, type);
				mapiType = Integer.parseInt(type, 16);
			} catch (RuntimeException re) {
				LOGGER.error("Could not parse directory entry {}", name, re);
				return new OutlookFieldInformation();
			}
			return new OutlookFieldInformation(clazz, mapiType);
		} else {
			LOGGER.trace("Ignoring entry with name {}", name);
		}
		// we are not interested in the field
		// and return an empty OutlookFieldInformation object
		return new OutlookFieldInformation();
	}

	/**
	 * Creates an {@link OutlookAttachment} object based on
	 * the given directory entry. The entry may either
	 * point to an attached file or to an
	 * attached .msg file, which will be added
	 * as a {@link OutlookMsgAttachment} object instead.
	 *
	 * @param dir The directory entry containing the attachment document entry and some other document entries describing the attachment (name, extension, mime
	 *            type, ...)
	 * @param msg The {@link OutlookMessage} object that this attachment should be added to.
	 * @throws IOException Thrown if the attachment could not be parsed/read.
	 */
	private void parseAttachment(DirectoryEntry dir, OutlookMessage msg)
			throws IOException {

		OutlookFileAttachment attachment = new OutlookFileAttachment();

		// iterate through all document entries
		for (Iterator<?> iter = dir.getEntries(); iter.hasNext(); ) {
			Entry entry = (Entry) iter.next();
			if (entry.isDocumentEntry()) {

				// the document entry may contain information about the attachment
				DocumentEntry de = (DocumentEntry) entry;
				OutlookMessageProperty msgProp = getMessagePropertyFromDocumentEntry(de);

				// we provide the class and data of the document entry to the attachment.
				// The attachment implementation has to know the semantics of the field names
				attachment.setProperty(msgProp);
			} else {
				// a directory within the attachment directory entry  means that a .msg file is attached at this point.
				// we recursively parse this .msg file and add it as a OutlookMsgAttachment object to the current OutlookMessage object.
				OutlookMessage attachmentMsg = new OutlookMessage();
				OutlookMsgAttachment msgAttachment = new OutlookMsgAttachment(attachmentMsg);
				msg.addAttachment(msgAttachment);
				checkDirectoryEntry((DirectoryEntry) entry, attachmentMsg);
			}
		}

		// only if there was really an attachment, we add this object to the OutlookMessage object
		if (attachment.getSize() > -1) {
			msg.addAttachment(attachment);
		}
	}

	/**
	 * Setter for overriding the default {@link RTF2HTMLConverter}
	 * implementation which is used to get HTML code from an RTF body.
	 *
	 * @param rtf2htmlConverter The converter instance to be used.
	 */
	public void setRtf2htmlConverter(RTF2HTMLConverter rtf2htmlConverter) {
		this.rtf2htmlConverter = rtf2htmlConverter;
	}
}
