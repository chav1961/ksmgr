package chav1961.ksmgr.interfaces;

import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

// https://www.tutorialsteacher.com/https/ssl-certificate-format
// https://www.sslmentor.com/help/ssl-certificate-formats
@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.FileFormat/chav1961/ksmgr/i18n/i18n.xml")
public enum FileFormat implements ModuleAccessor {
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileformat.pem",tooltip="chav1961.ksmgr.interfaces.fileformat.pem.tt")
	PEM,
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileformat.pkcs_7",tooltip="chav1961.ksmgr.interfaces.fileformat.pkcs_7.tt")
	PKCS_7,
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileformat.der",tooltip="chav1961.ksmgr.interfaces.fileformat.der.tt")
	DER,
	@LocaleResource(value="chav1961.ksmgr.interfaces.fileformat.pkcs_12",tooltip="chav1961.ksmgr.interfaces.fileformat.pkcs_12.tt")
	PKCS_12;
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
