package org.simplejavamail.outlookmessageparser.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMessagePropertyTest {

	@Test
	public void stripsOnlyTrailingNulCharactersFromStringData() {
		OutlookMessageProperty property = new OutlookMessageProperty("3712", " content-id \0\0", 26);

		assertThat(property.getData()).isEqualTo(" content-id ");
	}

	@Test
	public void leavesBinaryDataUntouched() {
		byte[] data = {1, 0, 0};

		OutlookMessageProperty property = new OutlookMessageProperty("3701", data, data.length);

		assertThat(property.getData()).isSameAs(data);
	}
}
