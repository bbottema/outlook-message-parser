package org.simplejavamail.outlookmessageparser;

import org.junit.jupiter.api.Test;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeApplicationOctetStream;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeApplicationSmime;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeMultipartSigned;

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
	public void extractSmimeHeaderDetectsPkcs7MimeWithoutOptionalParameters() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: application/pkcs7-mime;\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeApplicationSmime.class);
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("application/pkcs7-mime");
		assertThat(smime.getSmimeType()).isNull();
		assertThat(smime.getSmimeName()).isNull();
	}

	@Test
	public void extractSmimeHeaderDetectsPkcs7MimeParametersInAnyOrder() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: Application/Pkcs7-Mime; name=\"smime.p7m\"; smime-type=enveloped-data\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeApplicationSmime.class);
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("application/pkcs7-mime");
		assertThat(smime.getSmimeType()).isEqualTo("enveloped-data");
		assertThat(smime.getSmimeName()).isEqualTo("smime.p7m");
	}

	@Test
	public void extractSmimeHeaderDetectsMultipartSignedWithPkcs7SignatureProtocol() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg,
				"Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha-512; boundary=\"------------ms060207010804070005060507\"\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeMultipartSigned.class);
		OutlookSmimeMultipartSigned smime = (OutlookSmimeMultipartSigned) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("multipart/signed");
		assertThat(smime.getSmimeProtocol()).isEqualTo("application/pkcs7-signature");
		assertThat(smime.getSmimeMicalg()).isEqualTo("sha-512");
	}

	@Test
	public void extractSmimeHeaderDetectsFoldedMultipartSignedHeader() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg,
				"Content-Type: multipart/signed;\n"
						+ "\tprotocol=\"application/pkcs7-signature\";\n"
						+ "\tmicalg=sha-256\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeMultipartSigned.class);
		OutlookSmimeMultipartSigned smime = (OutlookSmimeMultipartSigned) msg.getSmime();
		assertThat(smime.getSmimeProtocol()).isEqualTo("application/pkcs7-signature");
		assertThat(smime.getSmimeMicalg()).isEqualTo("sha-256");
	}

	@Test
	public void extractSmimeHeaderIgnoresMultipartSignedWithOtherProtocol() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: multipart/signed; protocol=\"application/plain\"; micalg=sha-512\n");

		assertThat(msg.getSmime()).isNull();
	}

	@Test
	public void extractSmimeHeaderDetectsOctetStreamByContentTypeName() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: application/octet-stream; name=\"smime.p7m\"\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeApplicationOctetStream.class);
		OutlookSmimeApplicationOctetStream smime = (OutlookSmimeApplicationOctetStream) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("application/octet-stream");
		assertThat(smime.getSmimeName()).isEqualTo("smime.p7m");
	}

	@Test
	public void extractSmimeHeaderDetectsOctetStreamByContentDispositionFilename() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg,
				"Content-Type: application/octet-stream\n"
						+ "Content-Disposition: attachment; filename=\"signed.p7s\"\n");

		assertThat(msg.getSmime()).isInstanceOf(OutlookSmimeApplicationOctetStream.class);
		OutlookSmimeApplicationOctetStream smime = (OutlookSmimeApplicationOctetStream) msg.getSmime();
		assertThat(smime.getSmimeName()).isEqualTo("signed.p7s");
	}

	@Test
	public void extractSmimeHeaderIgnoresOctetStreamWithoutSmimeFilename() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: application/octet-stream; name=\"archive.zip\"\n");

		assertThat(msg.getSmime()).isNull();
	}

	@Test
	public void extractSmimeHeaderIgnoresBarePkcs7SignaturePart() {
		OutlookMessage msg = new OutlookMessage();

		OutlookMessageParser.extractSMimeHeader(msg, "Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\n");

		assertThat(msg.getSmime()).isNull();
	}
}
