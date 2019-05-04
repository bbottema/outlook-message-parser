package org.simplejavamail.outlookmessageparser.model;

// https://tools.ietf.org/html/rfc5751#page-32 (Identifying an S/MIME Message)
public abstract class OutlookSmime {
	
	public static class OutlookSmimeApplicationSmime extends OutlookSmime {
		private final String smimeMime;
		private final String smimeType;
		private final String smimeName;
		
		public OutlookSmimeApplicationSmime(String smimeMime, String smimeType, String smimeName) {
			this.smimeMime = smimeMime;
			this.smimeType = smimeType;
			this.smimeName = smimeName;
		}
		
		public String getSmimeMime() { return smimeMime; }
		public String getSmimeType() { return smimeType; }
		public String getSmimeName() { return smimeName; }
	}
	
	public static class OutlookSmimeMultipartSigned extends OutlookSmime {
		private final String smimeMime;
		private final String smimeProtocol;
		private final String smimeMicalg;
		
		public OutlookSmimeMultipartSigned(String smimeMime, String smimeProtocol, String smimeMicalg) {
			this.smimeMime = smimeMime;
			this.smimeProtocol = smimeProtocol;
			this.smimeMicalg = smimeMicalg;
		}
		
		public String getSmimeMime() { return smimeMime; }
		public String getSmimeProtocol() { return smimeProtocol; }
		public String getSmimeMicalg() { return smimeMicalg; }
	}
	
	public static class OutlookSmimeApplicationOctetStream extends OutlookSmime {
	
	}
}
