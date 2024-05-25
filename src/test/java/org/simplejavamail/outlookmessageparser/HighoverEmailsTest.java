package org.simplejavamail.outlookmessageparser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.Test;
import org.simplejavamail.outlookmessageparser.model.OutlookAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookFileAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookMessage;
import org.simplejavamail.outlookmessageparser.model.OutlookMessageAssert;
import org.simplejavamail.outlookmessageparser.model.OutlookMsgAttachment;
import org.simplejavamail.outlookmessageparser.model.OutlookRecipient;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeApplicationSmime;
import org.simplejavamail.outlookmessageparser.model.OutlookSmime.OutlookSmimeMultipartSigned;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.simplejavamail.outlookmessageparser.TestUtils.normalizeText;

public class HighoverEmailsTest {

	@Test
	public void testRtfSent()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/simple sent.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("John Doe");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("jdoes@someserver.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("(outlookEMLandMSGconverter Trial Version Import) BitDaddys Software");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("sales@bitdaddys.com", "sales@bitdaddys.com"));
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Dear BitDaddys Corp.,\n"
				+ "\n"
				+ "We have added your software to our approved list.\n"
				+ "\n"
				+ "Thank you for your efforts,\n"
				+ "Sincerely,\n"
				+ "John Doe\n"
				+ "Some Server Company\n");
	}

	@Test
	public void testToAndCCAreSeparated_Single()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/simple email with TO and CC_single.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Elias Laugher");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("elias.laugher@gmail.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Test E-Mail");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(
				createRecipient("Sven Sielenkemper", "sielenkemper@otris.de", "/o=otris/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=ad5e65c5e53d40e2825d738a39904682-sielenkemper"));
		OutlookMessageAssert.assertThat(msg).hasOnlyCcRecipients(
				createRecipient("niklas.lindson@gmail.com", "niklas.lindson@gmail.com"));
		OutlookMessageAssert.assertThat(msg).hasNoBccRecipients();
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Just a test to get an email with one cc recipient.\n");
	}

	// see https://github.com/bbottema/outlook-message-parser/issues/76
	@Test
	public void testDuplicateRecipientsBug()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/CC duplicate recipients bug.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Andrew McQuillen");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("atmcquillen@gmail.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Testing MSG");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(
				createRecipient("Andrew McQuillen", "andrew.mcquillen@civica.co.uk", "/o=ExchangeLabs/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=a1afd0d9b9254a1d8f0326f54698e43f-Andrew McQu"));
		OutlookMessageAssert.assertThat(msg).hasOnlyCcRecipients(
				createRecipient("Andrew McQuillen", "atmcquillen@gmail.com", "/o=ExchangeLabs/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=a1afd0d9b9254a1d8f0326f54698e43f-Andrew McQu"));
		OutlookMessageAssert.assertThat(msg).hasNoBccRecipients();
		assertThat(normalizeText(msg.getBodyText())).contains("This is an MSG message.");
		assertThat(msg.getHeadersMap()).doesNotContainKeys("CC", "Cc", "cc", "BCC", "Bcc", "bcc", "TO", "To", "to");
	}
	
	@Test
	public void testGithubIssue31_AttachmentNameWithSpecialCharacters()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/attachment with a bracket in the name.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName(null);
		OutlookMessageAssert.assertThat(msg).hasFromEmail(null);
		OutlookMessageAssert.assertThat(msg).hasSubject("Attachment with the name \"Attachment[1.pdf\"");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("a@b.c", "a@b.c"));
		OutlookMessageAssert.assertThat(msg).hasNoCcRecipients();
		OutlookMessageAssert.assertThat(msg).hasNoBccRecipients();
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		OutlookAttachment outlookAttachment1 = outlookAttachments.get(0);
		assertAttachmentMetadata(outlookAttachment1, "application/pdf", ".pdf", "", "Attachment[1.pdf");
		assertThat(outlookAttachment1).isOfAnyClassIn(OutlookFileAttachment.class);
		String attachmentContent1 = normalizeText(new String(((OutlookFileAttachment) outlookAttachment1).getData(), UTF_8));
		assertThat(attachmentContent1).isEqualTo("dummy attachment");
		assertThat(msg.fetchTrueAttachments()).hasSize(1);
		assertThat(msg.fetchTrueAttachments()).contains((OutlookFileAttachment) outlookAttachment1);
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Dummy text. Btw, the pdf is actually a .txt\n");
	}

	@Test
	public void testGithubIssue73_AttachmentNameWithSpecialCharacters()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/OutlookMessage with X500 dual address.msg");

		OutlookMessageAssert.assertThat(msg).hasFromName("Sven Sielenkemper");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("sielenkemper@otris.de");

		// just assert what currently comes back from the parser, not what the parser should do, because I'm not sure yet what the parser should do
		OutlookMessageAssert.assertThat(msg).hasOnlyRecipients(
				createRecipient("Sven Sielenkemper", "sielenkemper@otris.de",
						"/o=otris/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=ad5e65c5e53d40e2825d738a39904682-sielenkemper"),
				createRecipient("sven.sielenkemper@gmail.com", "sven.sielenkemper@gmail.com")
		);
	}

	@Test
	public void testToAndCCAreSeparated_Multiple()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/simple email with TO and CC_multiple.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("elias.laugher@gmail.com");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("elias.laugher@gmail.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Test E-Mail");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(
				createRecipient("elias.laugher@gmail.com", "elias.laugher@gmail.com"),
				createRecipient("niklas.lindson@gmail.com", "niklas.lindson@gmail.com"));
		OutlookMessageAssert.assertThat(msg).hasOnlyCcRecipients(
				createRecipient("egi.champi.titu@gmail.com", "egi.champi.titu@gmail.com"),
				createRecipient("egi.han.tzu@gmail.com", "egi.han.tzu@gmail.com"));
		OutlookMessageAssert.assertThat(msg).hasOnlyBccRecipients(
				createRecipient("egi.carn.carby@gmail.com", "egi.carn.carby@gmail.com"),
				createRecipient("egi.dink.meeker@gmail.com", "egi.dink.meeker@gmail.com"));
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Just a test to get an email with multiple to/cc/bcc recipients.\n\n \n\n");
	}
	
	@Test
	public void testUnsentRtfDraft()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/unsent draft.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName(null);
		OutlookMessageAssert.assertThat(msg).hasFromEmail(null);
		OutlookMessageAssert.assertThat(msg).hasSubject("MSG Test File");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("time2talk@online-convert.com", "time2talk@online-convert.com"));
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("MSG test file\n"
				+ "Purpose: Provide example of this file type\n"
				+ "Document file type: MSG\n"
				+ "Version: 1.0\n"
				+ "Remark:\n"
				+ "\n"
				+ "Example content:\n"
				+ "The names \"John Doe\" for males, \"Jane Doe\" or \"Jane Roe\" for females,\n"
				+ "or \"Jonnie Doe\" and \"Janie Doe\" for children, or just \"Doe\"\n"
				+ "non-gender-specifically are used as placeholder names for a party whose\n"
				+ "true identity is unknown or must be withheld in a legal action, case, or\n"
				+ "discussion. The names are also used to refer to acorpse or hospital\n"
				+ "patient whose identity is unknown. This practice is widely used in the\n"
				+ "United States and Canada, but is rarely used in other English-speaking\n"
				+ "countries including the United Kingdom itself, from where the use of\n"
				+ "\"John Doe\" in a legal context originates. The names Joe Bloggs or John\n"
				+ "Smith are used in the UK instead, as well as in Australia and New\n"
				+ "Zealand. \n"
				+ "\n"
				+ "John Doe is sometimes used to refer to a typical male in other contexts\n"
				+ "as well, in a similar manner to John Q. Public, known in Great Britain\n"
				+ "as Joe Public, John Smith or Joe Bloggs. For example, the first name\n"
				+ "listed on a form is often John Doe, along with a fictional address or\n"
				+ "other fictional information to provide an example of how to fill in the\n"
				+ "form. The name is also used frequently in popular culture, for example\n"
				+ "in the Frank Capra film Meet John Doe. John Doe was also the name of a\n"
				+ "2002 American television series. \n"
				+ "\n"
				+ "Similarly, a child or baby whose identity is unknown may be referred to\n"
				+ "as Baby Doe. A notorious murder case in Kansas City, Missouri, referred\n"
				+ "to the baby victim as Precious Doe. Other unidentified female murder\n"
				+ "victims are Cali Doe and Princess Doe. Additional persons may be called\n"
				+ "James Doe, Judy Doe, etc. However, to avoid possible confusion, if two\n"
				+ "anonymous or unknown parties are cited in a specific case or action, the\n"
				+ "surnames Doe and Roe may be used simultaneously; for example, \"John Doe\n"
				+ "v. Jane Roe\". If several anonymous parties are referenced, they may\n"
				+ "simply be labelled John Doe #1, John Doe #2, etc. (the U.S. Operation\n"
				+ "Delego cited 21 (numbered) \"John Doe\"s) or labelled with other variants\n"
				+ "of Doe / Roe / Poe / etc. Other early alternatives such as John Stiles\n"
				+ "and Richard Miles are now rarely used, and Mary Major has been used in\n"
				+ "some American federal cases. \n"
				+ "\n"
				+ "File created by http://www.online-convert.com\n"
				+ "<http://www.online-convert.com> \n"
				+ "More example files: http://www.online-convert.com/file-type\n"
				+ "<http://www.online-convert.com/file-type> \n"
				+ "Text of Example content: Wikipedia\n"
				+ "<http://en.wikipedia.org/wiki/John_Doe> \n"
				+ "License: Attribution-ShareAlike 3.0 Unported\n"
				+ "<http://creativecommons.org/licenses/by-sa/3.0/> \n"
				+ "\n"
				+ "Feel free to use and share the file according to the license above.\n");
	}

	@Test
	public void testHtmlMessageChain()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/plain chain.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Robert Duncan");
		OutlookMessageAssert.assertThat(msg).hasFromEmail(null);
		OutlookMessageAssert.assertThat(msg).hasSubject("RE: [Redmine - Bug #10180] redmine not truncating emails properly");
		OutlookMessageAssert.assertThat(msg).hasRecipients(
				createRecipient("'applsoft-redmine@anca.com'", "applsoft-redmine@anca.com"),
				createRecipient("'rob@thegopedal.com'", "rob@thegopedal.com"));
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getConvertedBodyHTML()).isNotEmpty();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Another test email to obtain raw data for Jean-Philippe.\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Regards,\n"
				+ "\n"
				+ "Robert Duncan\n"
				+ "\n"
				+ "ph. 8277\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "From: applsoft-redmine@anca.com [mailto:applsoft-redmine@anca.com] \n"
				+ "Sent: Friday, 5 October 2012 12:45 PM\n"
				+ "To: robertd@anca.com; Eng Tan\n"
				+ "Subject: [Redmine - Bug #10180] redmine not truncating emails properly\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Issue #10180 has been updated by Robert Duncan. \n"
				+ "\n"
				+ "Reported to redmine http://www.redmine.org/issues/12025\n"
				+ "\n"
				+ "________________________________\n"
				+ "\n"
				+ "\n"
				+ "Bug #10180: redmine not truncating emails properly\n"
				+ "<http://anca-redmine.anca.com.au/applsoft/issues/10180#change-5787> \n"
				+ "\n"
				+ "\n"
				+ "*\tAuthor: Robert Duncan\n"
				+ "*\tStatus: New\n"
				+ "*\tPriority: Normal\n"
				+ "*\tAssignee: Robert Duncan\n"
				+ "*\tCategory: \n"
				+ "*\tTarget version: \n"
				+ "\n"
				+ "Redmine should truncate emails after lines such as \"Regards,\". \n"
				+ "It did not do this, and included the entire email as a one-line comment\n"
				+ "for the issue.\n"
				+ "\n"
				+ "________________________________\n"
				+ "\n"
				+ "You have received this notification because you have either subscribed\n"
				+ "to it, or are involved in it.\n"
				+ "To change your notification preferences, please click here:\n"
				+ "http://anca-redmine.anca.com.au/applsoft/my/account\n\n");
	}

	@Test
	public void testNestedRtfMsg()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/nested simple mail.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("REISINGER Emanuel");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("Emanuel.Reisinger@cargonet.software");
		OutlookMessageAssert.assertThat(msg).hasSubject("outlookmsg2html Testmail");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("REISINGER Emanuel",
				"Emanuel.Reisinger@cargonet.software", "/o=ExchangeLabs/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=5512c5b0051348d992af993f92c30f5a-REISINGER E"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		OutlookAttachment outlookAttachment = outlookAttachments.get(0);
		assertThat(outlookAttachment).isOfAnyClassIn(OutlookMsgAttachment.class);
		OutlookMessage nestedMsg = ((OutlookMsgAttachment) outlookAttachment).getOutlookMessage();
		/* nested message */
		OutlookMessageAssert.assertThat(nestedMsg).hasFromName("REISINGER Emanuel");
		OutlookMessageAssert.assertThat(nestedMsg).hasFromEmail("Emanuel.Reisinger@cargonet.software");
		OutlookMessageAssert.assertThat(nestedMsg).hasSubject("outlookmsg2html Testmail");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("REISINGER Emanuel", "Emanuel.Reisinger@cargonet.software",
				"/o=ExchangeLabs/ou=Exchange Administrative Group (FYDIBOHF23SPDLT)/cn=Recipients/cn=5512c5b0051348d992af993f92c30f5a-REISINGER E"));
		OutlookMessageAssert.assertThat(nestedMsg).hasNoOutlookAttachments();
		assertThat(nestedMsg.getBodyText()).isNotEmpty();
		assertThat(nestedMsg.getBodyHTML()).isNull();
		assertThat(nestedMsg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(nestedMsg.getBodyText())).isEqualTo("Hello all,\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "This is a testmail.\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "BR\n"
				+ "\n"
				+ "Hui Pui \n"
				+ "\n");
		/* /nested message */
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getConvertedBodyHTML()).isNotEmpty();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Mail in mail.\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "BR\n"
				+ "\n");
	}

	@Test
	public void testFileAttachments()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/attachments.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Microsoft Outlook");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("MicrosoftExchange329e71ec88ae4615bbc36ab6ce41109e@coab.us");
		OutlookMessageAssert.assertThat(msg).hasSubject("Delivery delayed:RE: Bosco Fraud Cases [ 2 of 8]");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("darlington@coab.us", "darlington@coab.us"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(2);
		OutlookAttachment outlookAttachment1 = outlookAttachments.get(0);
		OutlookAttachment outlookAttachment2 = outlookAttachments.get(1);
		assertAttachmentMetadata(outlookAttachment1, "message/delivery-status", null, null, null);
		assertAttachmentMetadata(outlookAttachment2, "text/rfc822-headers", null, null, null);
		assertThat(outlookAttachment1).isOfAnyClassIn(OutlookFileAttachment.class);
		assertThat(outlookAttachment2).isOfAnyClassIn(OutlookFileAttachment.class);
		String attachmentContent1 = normalizeText(new String(((OutlookFileAttachment) outlookAttachment1).getData(), UTF_8));
		String attachmentContent2 = normalizeText(new String(((OutlookFileAttachment) outlookAttachment2).getData(), UTF_8));
		assertThat(attachmentContent1).isEqualTo("Reporting-MTA: dns;ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Received-From-MTA: dns;ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Arrival-Date: Mon, 11 Apr 2016 13:08:47 +0000\n"
				+ "\n"
				+ "Final-Recipient: rfc822;Darren.vuzzo@myfloridalicence.com\n"
				+ "Action: delayed\n"
				+ "Status: 4.4.7\n"
				+ "Diagnostic-Code: smtp;400 4.4.7 Message delayed\n"
				+ "Will-Retry-Until: Wed, 13 Apr 2016 09:08:47 -0400\n"
				+ "X-Display-Name: Darren.vuzzo@myfloridalicence.com\n");
		assertThat(attachmentContent2).isEqualTo("Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP Server\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563])\n"
				+ " by ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563%17]) with\n"
				+ " mapi id 15.00.1076.000; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "From: \"Arlington, Daniel\" <darlington@coab.us>\n"
				+ "To: ERICKSON ENERGY <ericksonenergy@gmail.com>, \"Reagan, Raina\"\n"
				+ "\t<Raina.Reagan@myfloridalicense.com>\n"
				+ "CC: \"Darren.vuzzo@myfloridalicence.com\" <Darren.vuzzo@myfloridalicence.com>,\n"
				+ "\t\"Rentfrow, Sandra\" <Sandra.Rentfrow@myfloridalicense.com>, Don Ford\n"
				+ "\t<dford@neptune-beach.com>, \"Taylor, Rick\" <RATaylor@coj.net>,\n"
				+ "\t\"JDouglas@coj.com\" <JDouglas@coj.com>, \"hwhite@sjcfl.us\" <hwhite@sjcfl.us>,\n"
				+ "\t\"building@jaxbchfl.net\" <building@jaxbchfl.net>, Jessica Bazanos\n"
				+ "\t<jessica.bazanos@gmail.com>\n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Topic: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Index: AQHRkNlJKh6lDOIrV0G4sEGvouPLo5+EwThA\n"
				+ "Date: Mon, 11 Apr 2016 13:08:46 +0000\n"
				+ "Message-ID: <73ade2cb375f4f39b9e210a57d087900@ABMAIL13.ci.atlantic-beach.fl.us>\n"
				+ "References: <CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "In-Reply-To: <CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "Accept-Language: en-US\n"
				+ "Content-Language: en-US\n"
				+ "X-MS-Has-Attach:\n"
				+ "X-MS-TNEF-Correlator:\n"
				+ "x-ms-exchange-transport-fromentityheader: Hosted\n"
				+ "x-originating-ip: [10.10.10.117]\n"
				+ "Content-Type: multipart/alternative;\n"
				+ "\tboundary=\"_000_73ade2cb375f4f39b9e210a57d087900ABMAIL13ciatlanticbeach_\"\n"
				+ "MIME-Version: 1.0\n");
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).isNotEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("Delivery is delayed to these recipients or groups:\n"
				+ "\n"
				+ "Darren.vuzzo@myfloridalicence.com (Darren.vuzzo@myfloridalicence.com)\n"
				+ "<mailto:Darren.vuzzo@myfloridalicence.com> \n"
				+ "\n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "\n"
				+ "This message hasn't been delivered yet. Delivery will continue to be\n"
				+ "attempted.\n"
				+ "\n"
				+ "The server will keep trying to deliver this message for the next 1 days,\n"
				+ "19 hours and 57 minutes. You'll be notified if the message can't be\n"
				+ "delivered by that time.\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "Diagnostic information for administrators:\n"
				+ "\n"
				+ "Generating server: ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Receiving server: myfloridalicence.com (208.73.211.178)\n"
				+ "\n"
				+ "\n"
				+ "Darren.vuzzo@myfloridalicence.com\n"
				+ "Remote Server at myfloridalicence.com (208.73.211.178) returned '400\n"
				+ "4.4.7 Message delayed'\n"
				+ "4/11/2016 4:58:21 PM - Remote Server at myfloridalicence.com\n"
				+ "(208.73.211.178) returned '441 4.4.1 Error encountered while\n"
				+ "communicating with primary target IP address: \"Failed to connect.\n"
				+ "Winsock error code: 10060, Win32 error code: 10060.\" Attempted failover\n"
				+ "to alternate host, but that did not succeed. Either there are no\n"
				+ "alternate hosts, or delivery failed to all alternate hosts. The last\n"
				+ "endpoint attempted was 208.73.211.178:25'\n"
				+ "\n"
				+ "Original message headers:\n"
				+ "\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP\n"
				+ "Server\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "([fe80::3d58:a9bc:50f6:e563])\n"
				+ " by ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563%17])\n"
				+ "with\n"
				+ " mapi id 15.00.1076.000; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "From: \"Arlington, Daniel\" <darlington@coab.us>\n"
				+ "To: ERICKSON ENERGY <ericksonenergy@gmail.com>, \"Reagan, Raina\"\n"
				+ "\t<Raina.Reagan@myfloridalicense.com>\n"
				+ "CC: \"Darren.vuzzo@myfloridalicence.com\"\n"
				+ "<Darren.vuzzo@myfloridalicence.com>,\n"
				+ "\t\"Rentfrow, Sandra\" <Sandra.Rentfrow@myfloridalicense.com>, Don\n"
				+ "Ford\n"
				+ "\t<dford@neptune-beach.com>, \"Taylor, Rick\" <RATaylor@coj.net>,\n"
				+ "\t\"JDouglas@coj.com\" <JDouglas@coj.com>, \"hwhite@sjcfl.us\"\n"
				+ "<hwhite@sjcfl.us>,\n"
				+ "\t\"building@jaxbchfl.net\" <building@jaxbchfl.net>, Jessica Bazanos\n"
				+ "\t<jessica.bazanos@gmail.com>\n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Topic: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Index: AQHRkNlJKh6lDOIrV0G4sEGvouPLo5+EwThA\n"
				+ "Date: Mon, 11 Apr 2016 13:08:46 +0000\n"
				+ "Message-ID:\n"
				+ "<73ade2cb375f4f39b9e210a57d087900@ABMAIL13.ci.atlantic-beach.fl.us>\n"
				+ "References:\n"
				+ "<CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "In-Reply-To:\n"
				+ "<CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "Accept-Language: en-US\n"
				+ "Content-Language: en-US\n"
				+ "X-MS-Has-Attach:\n"
				+ "X-MS-TNEF-Correlator:\n"
				+ "x-ms-exchange-transport-fromentityheader: Hosted\n"
				+ "x-originating-ip: [10.10.10.117]\n"
				+ "Content-Type: multipart/alternative;\n"
				+ "\t\n"
				+ "boundary=\"_000_73ade2cb375f4f39b9e210a57d087900ABMAIL13ciatlanticbeach_\"\n"
				+ "MIME-Version: 1.0\n"
				+ "\n");
	}
	
	@Test
	public void testEmbeddedImage()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/embedded image.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Paliarik, Martin");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("mpaliarik@mdlz.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("email test");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("Paliarik, Martin", "mpaliarik@mdlz.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		assertAttachmentMetadata(outlookAttachments.get(0), "image/png", ".png", "image001.png", "image001.png");
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getBodyRTF()).contains("cid:image001.png");
		assertThat(msg.fetchCIDMap()).hasSize(1);
		assertThat(msg.fetchCIDMap()).containsEntry("image001.png@01CDC1CA.B3005370", (OutlookFileAttachment) outlookAttachments.get(0));
		assertThat(msg.fetchTrueAttachments()).isEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).contains("This should pass into the kayako\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Martin Paliarik \n"
				+ "Compliance Management Administrator | Procurement Services\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Kraft Foods Europe Procurement GmbH – organizačná zložka | Racianska 44\n"
				+ "| 814 99  Bratislava | Slovakia \n"
				+ "\n"
				+ "Phone: +421 2 494 01 592 | mpaliarik@mdlz.com\n"
				+ "<mailto:mpaliarik@mdlz.com>  | www.mondelezinternational.com\n"
				+ "<http://www.mondelezinternational.com/> \n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "We are a member of the Mondelēz International family of companies from 2\n"
				+ "October 2012, \n"
				+ "but we will still use the name Kraft Foods until our legal entity\n"
				+ "changes in April 2013.\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Description: Description: Description: Description: Description:\n"
				+ "Description: Description: Q:\\062 Kraft Foods C&G Affairs Mondelez Launch\n"
				+ "2012-08\\E-Mail Signatur\\Bilder\\mdlz_rgb_logo_full_e-mail.png\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "Click here\n"
				+ "<mailto:EBSC-Feedback@mdlz.com?subject=Your%20Opinion%20Counts!>  to\n"
				+ "provide a feedback in English about our services\n"
				+ "\n"
				+ " \n"
				+ "\n"
				+ "The Information contained in this e-mail, and any files transmitted with\n"
				+ "it, is confidential and may be legally privileged.  It is intended\n"
				+ "solely for the addressee.  If you are not the intended recipient, please\n"
				+ "return the message by replying to it and then delete the message from\n"
				+ "your computer.  Any disclosure, copying, distribution or action taken in\n"
				+ "reliance on its contents is prohibited and may be unlawful.\n"
				+ "\n"
				+ " \n"
				+ "\n");
	}
	
	@Test
	public void testChineseMessage()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/chinese message.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName(null);
		OutlookMessageAssert.assertThat(msg).hasFromEmail("haozl@Ctrip.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("anjos@163.com", "anjos@163.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).isEmpty();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getConvertedBodyHTML()).isNotEmpty();
		assertThat(msg.fetchCIDMap()).isEmpty();
		assertThat(msg.fetchTrueAttachments()).isEmpty();
		assertThat(msg.getClientSubmitTime()).isNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo(" \n" +
				" \n" +
				"Dears：\n" +
				"      经过汇总 大家提前合理安排进房间入住吧。\n" +
				" \n" +
				"房型\n" +
				"时间段\n" +
				"迷你房\n" +
				"全天\n" +
				"大床房\n" +
				"9:00-12:00\n" +
				"小床房\n" +
				"1:00-15:30\n" +
				"标准房\n" +
				"15:30-18:00\n" +
				"三床房\n" +
				"全天\n" +
				"四床房\n" +
				"全天\n" +
				" \n" +
				" \n" +
				"Best Regards\n" +
				"Tha你\n" +
				"---------------------------------\n" +
				"郝竹林\n" +
				"Seat: 15#3F093 | TEL: 737057\n" +
				"酒店研发部\n" +
				" \n");
	}
	
	@Test
	public void testUnicodeMessage()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/tst_unicode.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("m.kalejs@outlook.com");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("m.kalejs@outlook.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Testcase");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("doesnotexist@doesnt.com", "doesnotexist@doesnt.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).isEmpty();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.fetchCIDMap()).isEmpty();
		assertThat(msg.fetchTrueAttachments()).isEmpty();
		assertThat(msg.getClientSubmitTime()).isNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("-/-\n" +
				"Char-å-Char\n" +
				"-/-\n" +
				"Char-Å-Char\n" +
				"-/-\n" +
				"Char-ø-Char\n" +
				"-/-\n" +
				"Char-Ø-Char\n" +
				"-/-\n" +
				"Char-æ-Char\n" +
				"-/-\n" +
				"Char-Æ-Char\n" +
				" \n");
	}
	
	@Test
	public void testToCcBcc()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/simple reply with CC.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("Benny Bottema");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("benny@bennybottema.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Re: Sent by CV Contact Form");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("David Johnston", "davidjono555@gmail.com"));
		OutlookMessageAssert.assertThat(msg).hasOnlyCcRecipients(
				createRecipient("Benny Bottema", "b.bottema@projectnibble.org"),
				createRecipient("Benny Bottema", "b.bottema@gmail.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).isEmpty();
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.fetchCIDMap()).isEmpty();
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(msg.fetchTrueAttachments()).isEmpty();
	}

	@Test
	public void testMsgForwardDraftWithBothAttachmentsAndEmbeddedImage()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/forward with attachments and embedded images.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName(null);
		OutlookMessageAssert.assertThat(msg).hasFromEmail(null);
		OutlookMessageAssert.assertThat(msg).hasSubject("FW: Delivery delayed:RE: Bosco Fraud Cases [ 2 of 8]");
		OutlookMessageAssert.assertThat(msg).hasNoToRecipients();
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(4);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		OutlookFileAttachment outlookAttachment2 = (OutlookFileAttachment) outlookAttachments.get(1);
		OutlookFileAttachment outlookAttachment3 = (OutlookFileAttachment) outlookAttachments.get(2);
		OutlookFileAttachment outlookAttachment4 = (OutlookFileAttachment) outlookAttachments.get(3);
		assertAttachmentMetadata(outlookAttachment1, "message/delivery-status", "", "", "");
		assertAttachmentMetadata(outlookAttachment2, "image/png", ".png", "image001.png", "image001.png");
		assertAttachmentMetadata(outlookAttachment3, "image/png", ".png", "image002.png", "image002.png");
		assertAttachmentMetadata(outlookAttachment4, "text/rfc822-headers", "", "", "");

		assertThat(msg.fetchCIDMap()).hasSize(2);
		assertThat(msg.fetchCIDMap()).containsEntry("image001.png@01D2951D.C5191030", outlookAttachment2);
		assertThat(msg.fetchCIDMap()).containsEntry("image002.png@01D2951D.C5191030", outlookAttachment3);
		assertThat(msg.fetchTrueAttachments()).hasSize(2);
		assertThat(msg.fetchTrueAttachments()).contains(outlookAttachment1, outlookAttachment4);

		assertThat(outlookAttachment1).isOfAnyClassIn(OutlookFileAttachment.class);
		assertThat(outlookAttachment4).isOfAnyClassIn(OutlookFileAttachment.class);
		String attachmentContent1 = normalizeText(new String(outlookAttachment1.getData(), UTF_8));
		String attachmentContent2 = normalizeText(new String(outlookAttachment4.getData(), UTF_8));
		assertThat(attachmentContent1).isEqualTo("Reporting-MTA: dns;ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Received-From-MTA: dns;ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Arrival-Date: Mon, 11 Apr 2016 13:08:47 +0000\n"
				+ "\n"
				+ "Final-Recipient: rfc822;Darren.vuzzo@myfloridalicence.com\n"
				+ "Action: delayed\n"
				+ "Status: 4.4.7\n"
				+ "Diagnostic-Code: smtp;400 4.4.7 Message delayed\n"
				+ "Will-Retry-Until: Wed, 13 Apr 2016 09:08:47 -0400\n"
				+ "X-Display-Name: Darren.vuzzo@myfloridalicence.com\n");
		assertThat(attachmentContent2).isEqualTo("Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP Server\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563])\n"
				+ " by ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563%17]) with\n"
				+ " mapi id 15.00.1076.000; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "From: \"Arlington, Daniel\" <darlington@coab.us>\n"
				+ "To: ERICKSON ENERGY <ericksonenergy@gmail.com>, \"Reagan, Raina\"\n"
				+ "\t<Raina.Reagan@myfloridalicense.com>\n"
				+ "CC: \"Darren.vuzzo@myfloridalicence.com\" <Darren.vuzzo@myfloridalicence.com>,\n"
				+ "\t\"Rentfrow, Sandra\" <Sandra.Rentfrow@myfloridalicense.com>, Don Ford\n"
				+ "\t<dford@neptune-beach.com>, \"Taylor, Rick\" <RATaylor@coj.net>,\n"
				+ "\t\"JDouglas@coj.com\" <JDouglas@coj.com>, \"hwhite@sjcfl.us\" <hwhite@sjcfl.us>,\n"
				+ "\t\"building@jaxbchfl.net\" <building@jaxbchfl.net>, Jessica Bazanos\n"
				+ "\t<jessica.bazanos@gmail.com>\n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Topic: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Index: AQHRkNlJKh6lDOIrV0G4sEGvouPLo5+EwThA\n"
				+ "Date: Mon, 11 Apr 2016 13:08:46 +0000\n"
				+ "Message-ID: <73ade2cb375f4f39b9e210a57d087900@ABMAIL13.ci.atlantic-beach.fl.us>\n"
				+ "References: <CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "In-Reply-To: <CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com>\n"
				+ "Accept-Language: en-US\n"
				+ "Content-Language: en-US\n"
				+ "X-MS-Has-Attach:\n"
				+ "X-MS-TNEF-Correlator:\n"
				+ "x-ms-exchange-transport-fromentityheader: Hosted\n"
				+ "x-originating-ip: [10.10.10.117]\n"
				+ "Content-Type: multipart/alternative;\n"
				+ "\tboundary=\"_000_73ade2cb375f4f39b9e210a57d087900ABMAIL13ciatlanticbeach_\"\n"
				+ "MIME-Version: 1.0\n");
		assertThat(msg.getBodyRTF()).contains("cid:image001.png");
		assertThat(msg.getBodyRTF()).contains("cid:image002.png");
		assertThat(msg.getConvertedBodyHTML()).contains("cid:image001.png");
		assertThat(msg.getConvertedBodyHTML()).contains("cid:image002.png");
		assertThat(msg.getBodyText()).isNotEmpty();
		assertThat(msg.getBodyHTML()).isNull();
		assertThat(msg.getClientSubmitTime()).isNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo(" \n"
				+ " \n"
				+ "Van: Microsoft Outlook\n"
				+ "[mailto:MicrosoftExchange329e71ec88ae4615bbc36ab6ce41109e@coab.us] \n"
				+ "Verzonden: 11 April 2016 19:11\n"
				+ "Aan: darlington@coab.us\n"
				+ "Onderwerp: Delivery delayed:RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ " \n"
				+ "Delivery is delayed to these recipients or groups:\n"
				+ "Darren.vuzzo@myfloridalicence.com (Darren.vuzzo@myfloridalicence.com)\n"
				+ "<mailto:Darren.vuzzo@myfloridalicence.com> \n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "This message hasn't been delivered yet. Delivery will continue to be\n"
				+ "attempted.\n"
				+ "The server will keep trying to deliver this message for the next 1\n"
				+ "days, 19 hours and 57 minutes. You'll be notified if the message can't\n"
				+ "be delivered by that time.\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "Diagnostic information for administrators:\n"
				+ "Generating server: ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "Receiving server: myfloridalicence.com (208.73.211.178)\n"
				+ "Darren.vuzzo@myfloridalicence.com\n"
				+ "<mailto:Darren.vuzzo@myfloridalicence.com> \n"
				+ "Remote Server at myfloridalicence.com (208.73.211.178) returned '400\n"
				+ "4.4.7 Message delayed'\n"
				+ "4/11/2016 4:58:21 PM - Remote Server at myfloridalicence.com\n"
				+ "(208.73.211.178) returned '441 4.4.1 Error encountered while\n"
				+ "communicating with primary target IP address: \"Failed to connect.\n"
				+ "Winsock error code: 10060, Win32 error code: 10060.\" Attempted failover\n"
				+ "to alternate host, but that did not succeed. Either there are no\n"
				+ "alternate hosts, or delivery failed to all alternate hosts. The last\n"
				+ "endpoint attempted was 208.73.211.178:25'\n"
				+ "Original message headers:\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) by\n"
				+ " ABMAIL13.ci.atlantic-beach.fl.us (10.10.10.21) with Microsoft SMTP\n"
				+ "Server\n"
				+ " (TLS) id 15.0.1076.9; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "Received: from ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "([fe80::3d58:a9bc:50f6:e563])\n"
				+ " by ABMAIL13.ci.atlantic-beach.fl.us ([fe80::3d58:a9bc:50f6:e563%17])\n"
				+ "with\n"
				+ " mapi id 15.00.1076.000; Mon, 11 Apr 2016 09:08:47 -0400\n"
				+ "From: \"Arlington, Daniel\" <darlington@coab.us\n"
				+ "<mailto:darlington@coab.us> >\n"
				+ "To: ERICKSON ENERGY <ericksonenergy@gmail.com\n"
				+ "<mailto:ericksonenergy@gmail.com> >, \"Reagan, Raina\"\n"
				+ "        <Raina.Reagan@myfloridalicense.com\n"
				+ "<mailto:Raina.Reagan@myfloridalicense.com> >\n"
				+ "CC: \"Darren.vuzzo@myfloridalicence.com\n"
				+ "<mailto:Darren.vuzzo@myfloridalicence.com> \"\n"
				+ "<Darren.vuzzo@myfloridalicence.com\n"
				+ "<mailto:Darren.vuzzo@myfloridalicence.com> >,\n"
				+ "        \"Rentfrow, Sandra\" <Sandra.Rentfrow@myfloridalicense.com\n"
				+ "<mailto:Sandra.Rentfrow@myfloridalicense.com> >, Don Ford\n"
				+ "        <dford@neptune-beach.com <mailto:dford@neptune-beach.com> >,\n"
				+ "\"Taylor, Rick\" <RATaylor@coj.net <mailto:RATaylor@coj.net> >,\n"
				+ "        \"JDouglas@coj.com <mailto:JDouglas@coj.com> \" <JDouglas@coj.com\n"
				+ "<mailto:JDouglas@coj.com> >, \"hwhite@sjcfl.us <mailto:hwhite@sjcfl.us> \"\n"
				+ "<hwhite@sjcfl.us <mailto:hwhite@sjcfl.us> >,\n"
				+ "        \"building@jaxbchfl.net <mailto:building@jaxbchfl.net> \"\n"
				+ "<building@jaxbchfl.net <mailto:building@jaxbchfl.net> >, Jessica Bazanos\n"
				+ "        <jessica.bazanos@gmail.com <mailto:jessica.bazanos@gmail.com> >\n"
				+ "Subject: RE: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Topic: Bosco Fraud Cases [ 2 of 8]\n"
				+ "Thread-Index: AQHRkNlJKh6lDOIrV0G4sEGvouPLo5+EwThA\n"
				+ "Date: Mon, 11 Apr 2016 13:08:46 +0000\n"
				+ "Message-ID:\n"
				+ "<73ade2cb375f4f39b9e210a57d087900@ABMAIL13.ci.atlantic-beach.fl.us\n"
				+ "<mailto:73ade2cb375f4f39b9e210a57d087900@ABMAIL13.ci.atlantic-beach.fl.u\n"
				+ "s> >\n"
				+ "References:\n"
				+ "<CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com\n"
				+ "<mailto:CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.c\n"
				+ "om> >\n"
				+ "In-Reply-To:\n"
				+ "<CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.com\n"
				+ "<mailto:CAC+pJabatY3d=2QZJV3c62H0k=G+bySP5fjEMM=hU9YDZM2RvA@mail.gmail.c\n"
				+ "om> >\n"
				+ "Accept-Language: en-US\n"
				+ "Content-Language: en-US\n"
				+ "X-MS-Has-Attach:\n"
				+ "X-MS-TNEF-Correlator:\n"
				+ "x-ms-exchange-transport-fromentityheader: Hosted\n"
				+ "x-originating-ip: [10.10.10.117]\n"
				+ "Content-Type: multipart/alternative;\n"
				+ "\n"
				+ "boundary=\"_000_73ade2cb375f4f39b9e210a57d087900ABMAIL13ciatlanticbeach_\"\n"
				+ "MIME-Version: 1.0\n");
	}
	
	@Test
	public void testSmimeMsgSigned()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/S_MIME test message signed.msg");
		
		OutlookMessageAssert.assertThat(msg).hasFromName("Benny Bottema");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("benny@bennybottema.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("S/MIME test message signed");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("Benny Bottema", "benny@bennybottema.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		// Outlook overrode dresscode.txt, presumably because it was more than 8 character long??
		assertAttachmentMetadata(outlookAttachment1, "multipart/signed", null, "smime.p7s", null);
		
		assertThat(msg.fetchCIDMap()).hasSize(0);
		assertThat(msg.fetchTrueAttachments()).hasSize(1);
		
		OutlookSmimeMultipartSigned smime = (OutlookSmimeMultipartSigned) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("multipart/signed");
		assertThat(smime.getSmimeProtocol()).isEqualTo("application/pkcs7-signature");
		assertThat(smime.getSmimeMicalg()).isEqualTo("sha-512");
	}
	
	@Test
	public void testSmimeMsgEncrypted()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/S_MIME test message encrypted.msg");
		
		OutlookMessageAssert.assertThat(msg).hasFromName("Benny Bottema");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("benny@bennybottema.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("S/MIME test message encrypted");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("Benny Bottema", "benny@bennybottema.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		// Outlook overrode dresscode.txt, presumably because it was more than 8 character long??
		assertAttachmentMetadata(outlookAttachment1, "application/pkcs7-mime", null, "smime.p7m", "smime.p7m");
		
		assertThat(msg.fetchCIDMap()).hasSize(0);
		assertThat(msg.fetchTrueAttachments()).hasSize(1);
		
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("application/pkcs7-mime");
		assertThat(smime.getSmimeName()).isEqualTo("smime.p7m");
		assertThat(smime.getSmimeType()).isEqualTo("enveloped-data");
	}
	
	@Test
	public void testSmimeMsgSignedAndEncrypted()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/S_MIME test message signed & encrypted.msg");
		
		OutlookMessageAssert.assertThat(msg).hasFromName("Benny Bottema");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("benny@bennybottema.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("S/MIME test message signed & encrypted");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("Benny Bottema", "benny@bennybottema.com"));
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(1);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		// Outlook overrode dresscode.txt, presumably because it was more than 8 character long??
		assertAttachmentMetadata(outlookAttachment1, "application/pkcs7-mime", null, "smime.p7m", "smime.p7m");
		
		assertThat(msg.fetchCIDMap()).hasSize(0);
		assertThat(msg.fetchTrueAttachments()).hasSize(1);
		
		OutlookSmimeApplicationSmime smime = (OutlookSmimeApplicationSmime) msg.getSmime();
		assertThat(smime.getSmimeMime()).isEqualTo("application/pkcs7-mime");
		assertThat(smime.getSmimeName()).isEqualTo("smime.p7m");
		assertThat(smime.getSmimeType()).isEqualTo("enveloped-data");
	}
	
	@Test
	public void testHtmlTestWithReplyToAndAttachmentsPlusEmbeddedImage()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/HTML mail with replyto and attachment and embedded image.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("lollypop");
		// Google SMTP overrode this, Outlook recognized it as: Benny Bottema <b.bottema@gmail.com>; on behalf of; lollypop <b.bottema@projectnibble.org>
		OutlookMessageAssert.assertThat(msg).hasFromEmail("b.bottema@projectnibble.org");
		OutlookMessageAssert.assertThat(msg).hasSubject("hey");
		// Outlook overrode this when saving the .msg to match the mail account
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("Bottema, Benny", "benny.bottema@aegon.nl", "/o=aegon/ou=exchange administrative group (fydibohf23spdlt)/cn=recipients/cn=bottema, benny7d7"));
		OutlookMessageAssert.assertThat(msg).hasReplyToName("lollypop-replyto");
		OutlookMessageAssert.assertThat(msg).hasReplyToEmail("lo.pop.replyto@somemail.com");
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("We should meet up!\n");
		// Outlook overrode this value too OR converted the original HTML to RTF, from which OutlookMessageParser derived this HTML
		assertThat(normalizeText(msg.getConvertedBodyHTML())).isEqualTo(
				"<b>We should meet up!</b><img src=\"cid:thumbsup\">");
		// the RTF was probably created by Outlook based on the HTML when the message was saved
		assertThat(msg.getBodyRTF()).isNotEmpty();
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(3);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		OutlookFileAttachment outlookAttachment2 = (OutlookFileAttachment) outlookAttachments.get(1);
		OutlookFileAttachment embeddedImg = (OutlookFileAttachment) outlookAttachments.get(2);
		// Outlook overrode dresscode.txt, presumably because it was more than 8 character long??
		assertAttachmentMetadata(outlookAttachment1, "text/plain", ".txt", "dressc~1.txt", "dresscode.txt");
		assertAttachmentMetadata(outlookAttachment2, "text/plain", ".txt", "location.txt", "location.txt");
		assertAttachmentMetadata(embeddedImg, "image/png", "", "thumbsup", "thumbsup");

		assertThat(msg.fetchCIDMap()).hasSize(1);
		assertThat(msg.fetchCIDMap()).containsEntry("thumbsup", embeddedImg);
		assertThat(msg.fetchTrueAttachments()).hasSize(2);
		assertThat(msg.fetchTrueAttachments()).contains(outlookAttachment1, outlookAttachment2);
		
		assertThat(msg.getSmime()).isNull();

		String attachmentContent1 = normalizeText(new String(outlookAttachment1.getData(), UTF_8));
		String attachmentContent2 = normalizeText(new String(outlookAttachment2.getData(), UTF_8));
		assertThat(attachmentContent1).isEqualTo("Black Tie Optional");
		assertThat(attachmentContent2).isEqualTo("On the moon!");
	}
	
	@Test
	public void testAttachmentAndNestedOutlookMessageGitHubIssue32()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/testgetmsgAttch.msg");
		OutlookMessageAssert.assertThat(msg).hasFromName("zhangtianhua@longestech.com");
		// Google SMTP overrode this, Outlook recognized it as: Benny Bottema <b.bottema@gmail.com>; on behalf of; lollypop <b.bottema@projectnibble.org>
		OutlookMessageAssert.assertThat(msg).hasFromEmail("zhangtianhua@longestech.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("测试多人邮件抄送msg格式");
		// Outlook overrode this when saving the .msg to match the mail account
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(
				createRecipient("zhaopengfei", "zhaopengfei@longestech.com"),
				createRecipient("zhangtianhua", "zhangtianhua@longestech.com"));
		OutlookMessageAssert.assertThat(msg).hasOnlyCcRecipients(
				createRecipient("zhaopengfei", "zhaopengfei@longestech.com"),
				createRecipient("zhangtianhua", "zhangtianhua@longestech.com"));
		OutlookMessageAssert.assertThat(msg).hasReplyToName(null);
		OutlookMessageAssert.assertThat(msg).hasReplyToEmail(null);
		assertThat(msg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(msg.getBodyText())).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAA\n");
		// the RTF was probably created by Outlook based on the HTML when the message was saved
		assertThat(msg.getBodyRTF()).isNotEmpty();
		List<OutlookAttachment> outlookAttachments = msg.getOutlookAttachments();
		assertThat(outlookAttachments).hasSize(2);
		OutlookFileAttachment outlookAttachment1 = (OutlookFileAttachment) outlookAttachments.get(0);
		OutlookMsgAttachment outlookAttachment2 = (OutlookMsgAttachment) outlookAttachments.get(1);
		assertAttachmentMetadata(outlookAttachment1, "application/octet-stream", null, "剑来.jpg", "剑来.jpg");
		
		assertThat(msg.fetchCIDMap()).isEmpty();
		assertThat(msg.fetchTrueAttachments()).hasSize(1);
		assertThat(msg.fetchTrueAttachments()).containsOnly(outlookAttachment1);
		
		assertThat(msg.getSmime()).isNull();
		
		/*
			NESTED OUTLOOK MESSAGE
		 */
		
		OutlookMessage nestedMsg = outlookAttachment2.getOutlookMessage();
		
		OutlookMessageAssert.assertThat(nestedMsg).hasFromName("zhangtianhua@longestech.com");
		// Google SMTP overrode this, Outlook recognized it as: Benny Bottema <b.bottema@gmail.com>; on behalf of; lollypop <b.bottema@projectnibble.org>
		OutlookMessageAssert.assertThat(nestedMsg).hasFromEmail("zhangtianhua@longestech.com");
		OutlookMessageAssert.assertThat(nestedMsg).hasSubject("test mail");
		// Outlook overrode this when saving the .msg to match the mail account
		OutlookMessageAssert.assertThat(nestedMsg).hasOnlyToRecipients(createRecipient("zhaopengfei", "zhaopengfei@longestech.com"));
		OutlookMessageAssert.assertThat(nestedMsg).hasOnlyCcRecipients(createRecipient("zhangtianhua", "zhangtianhua@longestech.com"));
		OutlookMessageAssert.assertThat(nestedMsg).hasReplyToName(null);
		OutlookMessageAssert.assertThat(nestedMsg).hasReplyToEmail(null);
		assertThat(nestedMsg.getClientSubmitTime()).isNotNull();
		assertThat(normalizeText(nestedMsg.getBodyText())).isEqualTo("Test12346464\n");
		// the RTF was probably created by Outlook based on the HTML when the message was saved
		assertThat(nestedMsg.getBodyRTF()).isNotEmpty();
		assertThat(nestedMsg.getOutlookAttachments()).isEmpty();
		
		assertThat(nestedMsg.fetchCIDMap()).isEmpty();
		assertThat(nestedMsg.fetchTrueAttachments()).isEmpty();
		
		assertThat(nestedMsg.getSmime()).isNull();
	}
	
	@Test
	public void testQoutedNameswithAtSign()
			throws IOException {
		OutlookMessage msg = parseMsgFile("test-messages/Test at sign in personal From header.msg");
		
		OutlookMessageAssert.assertThat(msg).hasFromName("bogus@acme.com");
		OutlookMessageAssert.assertThat(msg).hasFromEmail("bogus@domain.com");
		OutlookMessageAssert.assertThat(msg).hasSubject("Test at sign in personal 3");
		OutlookMessageAssert.assertThat(msg).hasOnlyToRecipients(createRecipient("'recipient'", "recipient@domain.com"));
		
		OutlookMessageAssert.assertThat(msg).hasNoOutlookAttachments();
		assertThat(msg.fetchCIDMap()).hasSize(0);
		assertThat(msg.fetchTrueAttachments()).hasSize(0);
		assertThat((OutlookSmimeApplicationSmime) msg.getSmime()).isNull();
	}

	private void assertAttachmentMetadata(OutlookAttachment attachment, String mimeType, String fileExt, String filename, String fullname) {
		assertThat(attachment).isOfAnyClassIn(OutlookFileAttachment.class);
		assertThat(((OutlookFileAttachment) attachment).getMimeTag()).describedAs("mimeTag").isEqualTo(mimeType);
		assertThat(((OutlookFileAttachment) attachment).getExtension()).describedAs("extension").isEqualTo(fileExt);
		assertThat(((OutlookFileAttachment) attachment).getFilename()).describedAs("filename").isEqualTo(filename);
		assertThat(((OutlookFileAttachment) attachment).getLongFilename()).describedAs("longFilename").isEqualTo(fullname);
	}

	private static OutlookRecipient createRecipient(@Nullable String toName, @NotNull String toEmail) {
		return createRecipient(toName, toEmail, null);
	}

	private static OutlookRecipient createRecipient(@Nullable String toName, @NotNull String toEmail, @Nullable String x500Address) {
		final OutlookRecipient outlookRecipient = new OutlookRecipient();
		outlookRecipient.setName(toName);
		outlookRecipient.setAddress(requireNonNull(toEmail));
		outlookRecipient.setX500Address(x500Address);
		return outlookRecipient;
	}

	private static OutlookMessage parseMsgFile(String msgPath)
			throws IOException {
		InputStream resourceAsStream = OutlookMessageParser.class.getClassLoader().getResourceAsStream(msgPath);
		return new OutlookMessageParser().parseMsg(requireNonNull(resourceAsStream));
	}
}