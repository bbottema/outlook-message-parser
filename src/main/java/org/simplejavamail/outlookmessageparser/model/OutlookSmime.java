package org.simplejavamail.outlookmessageparser.model;

import javax.annotation.Nullable;
import java.util.Objects;

// https://tools.ietf.org/html/rfc5751#page-32 (Identifying an S/MIME Message)
public abstract class OutlookSmime {
	
	public static class OutlookSmimeApplicationSmime extends OutlookSmime {
		private final String smimeMime;
		private final String smimeType;
		private final String smimeName;
		
		public OutlookSmimeApplicationSmime(String smimeMime, @Nullable String smimeType, @Nullable String smimeName) {
			this.smimeMime = smimeMime;
			this.smimeType = smimeType;
			this.smimeName = smimeName;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OutlookSmimeApplicationSmime that = (OutlookSmimeApplicationSmime) o;
			return Objects.equals(smimeMime, that.smimeMime) &&
					Objects.equals(smimeType, that.smimeType) &&
					Objects.equals(smimeName, that.smimeName);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(smimeMime, smimeType, smimeName);
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("OutlookSmimeApplicationSmime{");
			sb.append("smimeMime='").append(smimeMime).append('\'');
			sb.append(", smimeType='").append(smimeType).append('\'');
			sb.append(", smimeName='").append(smimeName).append('\'');
			sb.append('}');
			return sb.toString();
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
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OutlookSmimeMultipartSigned that = (OutlookSmimeMultipartSigned) o;
			return Objects.equals(smimeMime, that.smimeMime) &&
					Objects.equals(smimeProtocol, that.smimeProtocol) &&
					Objects.equals(smimeMicalg, that.smimeMicalg);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(smimeMime, smimeProtocol, smimeMicalg);
		}
		
		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("OutlookSmimeMultipartSigned{");
			sb.append("smimeMime='").append(smimeMime).append('\'');
			sb.append(", smimeProtocol='").append(smimeProtocol).append('\'');
			sb.append(", smimeMicalg='").append(smimeMicalg).append('\'');
			sb.append('}');
			return sb.toString();
		}
		
		public String getSmimeMime() { return smimeMime; }
		public String getSmimeProtocol() { return smimeProtocol; }
		public String getSmimeMicalg() { return smimeMicalg; }
	}
	
	public static class OutlookSmimeApplicationOctetStream extends OutlookSmime {
		private final String smimeMime;
		private final String smimeProtocol;
		private final String smimeName;

		public OutlookSmimeApplicationOctetStream(String smimeMime, @Nullable String smimeProtocol, @Nullable String smimeName) {
			this.smimeMime = smimeMime;
			this.smimeProtocol = smimeProtocol;
			this.smimeName = smimeName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OutlookSmimeApplicationOctetStream that = (OutlookSmimeApplicationOctetStream) o;
			return Objects.equals(smimeMime, that.smimeMime) &&
					Objects.equals(smimeProtocol, that.smimeProtocol) &&
					Objects.equals(smimeName, that.smimeName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(smimeMime, smimeProtocol, smimeName);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("OutlookSmimeApplicationOctetStream{");
			sb.append("smimeMime='").append(smimeMime).append('\'');
			sb.append(", smimeProtocol='").append(smimeProtocol).append('\'');
			sb.append(", smimeName='").append(smimeName).append('\'');
			sb.append('}');
			return sb.toString();
		}

		public String getSmimeMime() { return smimeMime; }
		public String getSmimeProtocol() { return smimeProtocol; }
		public String getSmimeMicalg() { return smimeName; }
	}
}
