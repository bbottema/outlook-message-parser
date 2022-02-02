package org.simplejavamail.outlookmessageparser;

import org.junit.Test;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMessageParserTest {

	private static final String HEADERS = "Date: Sun, 5 Mar 2017 12:11:31 +0100\n"
			+ "Reply-To: lollypop-replyto <lo.pop.replyto@somemail.com>\n"
			+ "To: C.Cane <hola.planet@beaches.com>";

	@Test
	public void extractReplyToHeader() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, HEADERS);
		assertThat(msg.getReplyToName()).isEqualTo("lollypop-replyto");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToPartialAddressHeader() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, "Reply-To: <lo.pop.replyto@somemail.com>\n");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToPartialNameHeader() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, "Reply-To: lo.pop.replyto@somemail.com\n");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToHeaderMissing() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, "Date: Sun, 5 Mar 2017 12:11:31 +0100\n"
				+ "To: C.Cane <hola.planet@beaches.com>");
		assertThat(msg.getReplyToName()).isNull();
		assertThat(msg.getReplyToEmail()).isNull();
	}

	@Test
	public void extractReplyToHeaderMalformed1() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, "Reply-To: <lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToHeaderMalformed2() {
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractReplyToHeader(msg, "Reply-To: lo.pop.r<eplyto@somemail.com>tg>");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.r");
		assertThat(msg.getReplyToEmail()).isEqualTo("eplyto@somemail.com>tg");
	}

	@Test
	public void extractSmimePropertiesOctetStream() {

		/* application/octet-stream and file-suffix p7m, p7s, p7c, p7z */
		String header = "Content-Type: application/octet-stream; name=\"smime.p7m\"";
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		header = "Content-Type: application/octet-stream; name=\"smime.p7s\"";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		header = "Content-Type: application/octet-stream; name=\"smime.p7c\"";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		header = "Content-Type: application/octet-stream; name=\"smime.p7z\"";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		/* application/octet-stream and file-suffix NOT p7m, p7s, p7c, p7z */
		header = "Content-Type: application/octet-stream; name=\"smime.zip\"";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNull();

	}

	@Test
	public void extractSmimePropertiesMultipartSigned() {
		/*multipart/signed and protocol=pkcs7-signature*/
		String header = "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha-512; boundary=\"------------ms060207010804070005060507\"";
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		/*multipart/signed and protocol=x-pkcs7-signature*/
		header = "Content-Type: multipart/signed; protocol=\"application/x-pkcs7-signature\"; micalg=sha-256; boundary=\"------------ms060207010804070005060507\"";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		/*multipart/signed and some other protocol*/
		header = "Content-Type: multipart/signed; protocol=\"application/plain\"; micalg=sha-512";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNull();
	}

	@Test
	public void extractSmimePropertiesPkcsMime() {
		/* application/pkcs7-mime name and smime-type is optional */
		String header = "Content-Type: application/pkcs7-mime; name=\"smime.p7m\"; smime-type=enveloped-data";
		OutlookMessage msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();

		header = "Content-Type: application/pkcs7-mime";
		msg = new OutlookMessage();
		OutlookMessageParser.extractSMimeHeader(msg, header);
		assertThat(msg.getSmime()).isNotNull();
	}
}
