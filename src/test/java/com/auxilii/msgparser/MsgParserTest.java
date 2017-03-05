package com.auxilii.msgparser;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MsgParserTest {

	private static final String HEADERS = "Date: Sun, 5 Mar 2017 12:11:31 +0100\n"
			+ "Reply-To: lollypop-replyto <lo.pop.replyto@somemail.com>\n"
			+ "To: C.Cane <hola.planet@beaches.com>";

	@Test
	public void extractReplyToHeader() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, HEADERS);
		assertThat(msg.getReplyToName()).isEqualTo("lollypop-replyto");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToPartialAddressHeader() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, "Reply-To: <lo.pop.replyto@somemail.com>\n");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToPartialNameHeader() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, "Reply-To: lo.pop.replyto@somemail.com\n");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToHeaderMissing() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, "Date: Sun, 5 Mar 2017 12:11:31 +0100\n"
				+ "To: C.Cane <hola.planet@beaches.com>");
		assertThat(msg.getReplyToName()).isNull();
		assertThat(msg.getReplyToEmail()).isNull();
	}

	@Test
	public void extractReplyToHeaderMalformed1() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, "Reply-To: <lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.replyto@somemail.com");
		assertThat(msg.getReplyToEmail()).isEqualTo("lo.pop.replyto@somemail.com");
	}

	@Test
	public void extractReplyToHeaderMalformed2() {
		Message msg = new Message();
		MsgParser.extractReplyToHeader(msg, "Reply-To: lo.pop.r<eplyto@somemail.com>tg>");
		assertThat(msg.getReplyToName()).isEqualTo("lo.pop.r");
		assertThat(msg.getReplyToEmail()).isEqualTo("eplyto@somemail.com>tg");
	}
}