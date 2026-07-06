package org.simplejavamail.outlookmessageparser.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OutlookMsgAttachmentTest {

	@Test
	public void exposesAttachmentMetadataForNestedMessage() {
		OutlookFileAttachment attachment = new OutlookFileAttachment();
		attachment.setContentId("nested-content-id");
		attachment.setMimeTag("message/rfc822");
		attachment.setFilename("nested.msg");

		OutlookMsgAttachment subject = new OutlookMsgAttachment(new OutlookMessage(), attachment);

		assertThat(subject.getAttachment()).isSameAs(attachment);
		assertThat(subject.getAttachment().getContentId()).isEqualTo("nested-content-id");
		assertThat(subject.getAttachment().getMimeTag()).isEqualTo("message/rfc822");
		assertThat(subject.getAttachment().getFilename()).isEqualTo("nested.msg");
	}
}
