/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simplejavamail.outlookmessageparser.model;

import org.junit.Test;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeApplicationSmime;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMessageTest {
	
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
	
	private void testSmime(String smimeHeader, String smimeMime, String smimeType, String smimeName) {
		OutlookMessage msg = new OutlookMessage();
		msg.setSmimeApplicationSmime(smimeHeader);
		
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo(smimeMime);
		assertThat(smime.getSmimeType()).isEqualTo(smimeType);
		assertThat(smime.getSmimeName()).isEqualTo(smimeName);
	}
}