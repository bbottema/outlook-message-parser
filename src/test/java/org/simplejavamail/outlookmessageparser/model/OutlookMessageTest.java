package org.simplejavamail.outlookmessageparser.model;

import org.junit.jupiter.api.Test;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeApplicationSmime;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMessageTest {

	@Test
	public void absentMessageDateIsUnavailable() {
		assertThat(new OutlookMessage().getDate()).isNull();
	}

	@Test
	public void absentCreationDateIsUnavailable() {
		assertThat(new OutlookMessage().getCreationDate()).isNull();
	}

	@Test
	public void absentModificationDateIsUnavailable() {
		assertThat(new OutlookMessage().getLastModificationDate()).isNull();
	}

	@Test
	public void presentDatesRemainIndependentDefensiveCopies() {
		OutlookMessage msg = new OutlookMessage();
		msg.setProperty(property("3007", "Mon Jul 23 15:43:12 UTC 2012"), null);
		msg.setProperty(property("3008", "Tue Jul 24 16:44:13 UTC 2012"), null);
		msg.setProperty(property("0039", "Sun Jul 22 14:42:11 UTC 2012"), null);
		Date created = Date.from(Instant.parse("2012-07-23T15:43:12Z"));
		Date modified = Date.from(Instant.parse("2012-07-24T16:44:13Z"));
		Date submitted = Date.from(Instant.parse("2012-07-22T14:42:11Z"));

		assertThat(msg.getDate()).isEqualTo(created);
		assertThat(msg.getCreationDate()).isEqualTo(created);
		assertThat(msg.getLastModificationDate()).isEqualTo(modified);
		assertThat(msg.getClientSubmitTime()).isEqualTo(submitted);
		msg.getDate().setTime(0);
		msg.getCreationDate().setTime(0);
		msg.getLastModificationDate().setTime(0);
		msg.getClientSubmitTime().setTime(0);
		assertThat(msg.getDate()).isEqualTo(created);
		assertThat(msg.getCreationDate()).isEqualTo(created);
		assertThat(msg.getLastModificationDate()).isEqualTo(modified);
		assertThat(msg.getClientSubmitTime()).isEqualTo(submitted);
	}
	
	@Test
	public void testSetSmimeNull() {
		OutlookMessage msg = new OutlookMessage();
		msg.setSmimeApplicationSmime(null);
		assertThat(msg.getSmime()).isNull();
		msg.setSmimeApplicationSmime("");
		assertThat(msg.getSmime()).isNull();
		msg.setSmimeApplicationSmime("moomoo");
		assertThat(msg.getSmime()).isNull();
	}
	
	@Test
	public void testSetSmimeNonNull() {
		// application/pkcs7-mime;smime-type=signed-data;name=smime.p7m
		testSmime("application/pkcs7-mime", "application/pkcs7-mime", null, null);
		testSmime("application/pkcs7-mime;", "application/pkcs7-mime", null, null);
		testSmime("application/pkcs7-mime;name=moo", "application/pkcs7-mime", null, "moo");
		testSmime("application/pkcs7-mime;smime-type=signed-data;name=smime.p7m", "application/pkcs7-mime", "signed-data", "smime.p7m");
		testSmime("application/pkcs7-mime;name=smime.p7m;smime-type=signed-data", "application/pkcs7-mime", "signed-data", "smime.p7m");
		testSmime("application/pkcs7-mime;name=smime.p7m;smime-type=signed-data;", "application/pkcs7-mime", "signed-data", "smime.p7m");
	}

	@Test
	public void testGithubIssue77_LastModifierNameDoesNotBecomeSenderEmail() {
		OutlookMessage msg = new OutlookMessage();

		msg.setProperty(property("3ffa", "modifier@example.com"), null);

		assertThat(msg.getFromEmail()).isNull();
		assertThat(msg.getLastModifierName()).isEqualTo("modifier@example.com");
		assertThat(msg.getPropertyValue(0x3ffa)).isEqualTo("modifier@example.com");
	}

	@Test
	public void testGithubIssue77_LastModifierNameDoesNotOverwriteSenderEmail() {
		OutlookMessage msg = new OutlookMessage();

		msg.setProperty(property("0c1f", "sender@example.com"), null);
		msg.setProperty(property("3ffa", "modifier@example.com"), null);

		assertThat(msg.getFromEmail()).isEqualTo("sender@example.com");
		assertThat(msg.getLastModifierName()).isEqualTo("modifier@example.com");
	}

	@Test
	public void testGithubIssue77_X500SenderAddressDoesNotBecomeSenderEmail() {
		OutlookMessage msg = new OutlookMessage();

		msg.setProperty(property("0c1f", "/o=ExchangeLabs/ou=Exchange Administrative Group/cn=Recipients/cn=sender"), null);

		assertThat(msg.getFromEmail()).isNull();
	}

	@Test
	public void testGithubIssue77_ArbitrarySenderNameDoesNotBecomeSenderEmail() {
		OutlookMessage msg = new OutlookMessage();

		msg.setProperty(property("0c1f", "Unknown"), null);

		assertThat(msg.getFromEmail()).isNull();
	}
	
	private void testSmime(String smimeHeader, String smimeMime, String smimeType, String smimeName) {
		OutlookMessage msg = new OutlookMessage();
		msg.setSmimeApplicationSmime(smimeHeader);
		
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo(smimeMime);
		assertThat(smime.getSmimeType()).isEqualTo(smimeType);
		assertThat(smime.getSmimeName()).isEqualTo(smimeName);
	}

	private static OutlookMessageProperty property(String clazz, String data) {
		return new OutlookMessageProperty(clazz, data, data.length());
	}
}
