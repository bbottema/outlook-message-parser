package org.simplejavamail.outlookmessageparser.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMessageTest {
	@Test
	public void testSetSmime() {
		// application/pkcs7-mime;smime-type=signed-data;name=smime.p7m
		testSmime(null, null, null, null);
		testSmime("application/pkcs7-mime", "application/pkcs7-mime", null, null);
		testSmime("application/pkcs7-mime;", "application/pkcs7-mime", null, null);
		testSmime("application/pkcs7-mime;name=moo", "application/pkcs7-mime", null, "moo");
		testSmime("application/pkcs7-mime;smime-type=signed-data;name=smime.p7m", "application/pkcs7-mime", "signed-data", "smime.p7m");
		testSmime("application/pkcs7-mime;name=smime.p7m;smime-type=signed-data", "application/pkcs7-mime", "signed-data", "smime.p7m");
		testSmime("application/pkcs7-mime;name=smime.p7m;smime-type=signed-data;", "application/pkcs7-mime", "signed-data", "smime.p7m");
	}
	
	private void testSmime(String smimeHeader, String smimeMime, String smimeType, String smimeName) {
		OutlookMessage msg = new OutlookMessage();
		msg.setSmime(smimeHeader);
		assertThat(msg.getSmimeMime()).isEqualTo(smimeMime);
		assertThat(msg.getSmimeType()).isEqualTo(smimeType);
		assertThat(msg.getSmimeName()).isEqualTo(smimeName);
	}
}