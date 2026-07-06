package org.simplejavamail.outlookmessageparser.model;

import jakarta.activation.MimetypesFileTypeMap;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.assertj.core.api.Assertions.assertThat;

public class OutlookFileAttachmentTest {
	
	@Test
	public void checkSmimeFilename() {
		testSmimeFilenameScenario(null, "image/png", null);
		testSmimeFilenameScenario("file", "image/png", "file");
		testSmimeFilenameScenario("file", "multipart/signed", "file");
		testSmimeFilenameScenario(null, "multipart/signed", "smime.p7s");
		testSmimeFilenameScenario(null, "multipart/signed;protocol=\"moomoo\"", null);
		testSmimeFilenameScenario("file", "multipart/signed;protocol=\"moomoo\"", "file");
		testSmimeFilenameScenario(null, "multipart/signed;protocol=\"application/pkcs7-signature\"", "smime.p7s");
		testSmimeFilenameScenario("file", "multipart/signed;protocol=\"application/pkcs7-signature\"", "file");
	}
	
	private void testSmimeFilenameScenario(String filename, String mimeTag, String expectedNewFilename) {
		OutlookFileAttachment subject = new OutlookFileAttachment();
		subject.setFilename(filename);
		subject.setMimeTag(mimeTag);
		
		subject.checkSmimeFilename();
		assertThat(subject.getFilename()).isEqualTo(expectedNewFilename);
	}
	
	@Test
	public void checkMimeTag() {
		testMimeTagScenario("image/png", "image.png", "image/png");
		testMimeTagScenario("image/png", "image.bmp", "image/png");
		testMimeTagScenario("image/png", null, "image/png");
		testMimeTagScenario(null, null, null);
		testMimeTagScenario(null, "moo", "application/octet-stream");
		testMimeTagScenario(null, "image.png", "image/png");
		testMimeTagScenario(null, "image.bmp", "image/bmp");
	}

	@Test
	public void checkMimeTagPrefersLongFilenameOverShortFilename() {
		testMimeTagScenario(null, "report.xls", "report.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@Test
	public void checkMimeTagFallsBackToShortFilenameForUnknownLongFilename() {
		testMimeTagScenario(null, "image.png", "image", "image/png");
	}

	@Test
	public void checkExcelMimeTypes() {
		assertThat(MimeType.getContentType("report.xlsx")).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(MimeType.getContentType("report.xls")).isEqualTo("application/vnd.ms-excel");
		assertThat(MimeType.getContentType("report.xlsm")).isEqualTo("application/vnd.ms-excel.sheet.macroEnabled.12");
		assertThat(MimeType.getContentType("report.xltm")).isEqualTo("application/vnd.ms-excel.template.macroEnabled.12");
		assertThat(MimeType.getContentType("report.xlam")).isEqualTo("application/vnd.ms-excel.addin.macroEnabled.12");
		assertThat(MimeType.getContentType("report.xlsb")).isEqualTo("application/vnd.ms-excel.sheet.binary.macroEnabled.12");
		assertThat(MimeType.getContentType("report.xltx")).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.template");
	}

	@Test
	public void createMimeTypeMapTriesFallbackResourceLoader() {
		MimetypesFileTypeMap map = MimeType.createMap(
				() -> null,
				() -> new ByteArrayInputStream("application/x-test testext".getBytes(US_ASCII)));

		assertThat(map.getContentType("attachment.testext")).isEqualTo("application/x-test");
	}

	@Test
	public void createMimeTypeMapFallsBackToActivationDefaults() {
		MimetypesFileTypeMap map = MimeType.createMap(() -> null);

		assertThat(map.getContentType("attachment.unknown-extension")).isEqualTo("application/octet-stream");
	}

	private void testMimeTagScenario(String mimeTag, String filename, String expectedNewMimeTag) {
		testMimeTagScenario(mimeTag, filename, null, expectedNewMimeTag);
	}

	private void testMimeTagScenario(String mimeTag, String filename, String longFilename, String expectedNewMimeTag) {
		OutlookFileAttachment subject = new OutlookFileAttachment();
		subject.setMimeTag(mimeTag);
		subject.setFilename(filename);
		subject.setLongFilename(longFilename);
		
		subject.checkMimeTag();
		assertThat(subject.getMimeTag()).isEqualTo(expectedNewMimeTag);
	}
}
