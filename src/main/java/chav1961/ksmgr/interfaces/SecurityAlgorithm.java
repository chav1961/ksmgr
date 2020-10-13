package chav1961.ksmgr.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.SecurityAlgorithm/chav1961/ksmgr/i18n/i18n.xml")
public enum SecurityAlgorithm {
	@LocaleResource(value="chav1961.ksmgr.interfaces.securityalgorithm.rsa",tooltip="chav1961.ksmgr.interfaces.securityalgorithm.rsa.tt")
	RSA("RSA"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.securityalgorithm.bc",tooltip="chav1961.ksmgr.interfaces.securityalgorithm.bc.tt")
	BC("BC"),
	@LocaleResource(value="chav1961.ksmgr.interfaces.securityalgorithm.des",tooltip="chav1961.ksmgr.interfaces.securityalgorithm.des.tt")
	DES("DES");
	
	private final String	algoName;
	
	SecurityAlgorithm(final String algoName) {
		this.algoName = algoName;
	}
	
	public String getAlgorithm() {
		return algoName;
	}
}
