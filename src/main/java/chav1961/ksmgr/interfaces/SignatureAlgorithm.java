package chav1961.ksmgr.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.SignatureAlgorithm/chav1961/ksmgr/i18n/i18n.xml")
public enum SignatureAlgorithm {
	@LocaleResource(value="chav1961.ksmgr.interfaces.signaturealgorithm.sha1withrsa",tooltip="chav1961.ksmgr.interfaces.signaturealgorithm.sha1withrsa.tt")
	SHA1withRSA("SHA1withRSA");
	
	private final String	algoName;
	
	SignatureAlgorithm(final String algoName) {
		this.algoName = algoName;
	}
	
	public String getAlgorithm() {
		return algoName;
	}
	
}
