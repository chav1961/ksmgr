package chav1961.ksmgr.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.ImportEntityType/chav1961/ksmgr/i18n/i18n.xml")
public enum ImportEntityType {
	@LocaleResource(value="chav1961.ksmgr.interfaces.importentitytype.secretkey",tooltip="chav1961.ksmgr.interfaces.importentitytype.secretkey.tt")
	SECRET_KEY,
	@LocaleResource(value="chav1961.ksmgr.interfaces.importentitytype.keypair",tooltip="chav1961.ksmgr.interfaces.importentitytype.keypair.tt")
	KEY_PAIR,
	@LocaleResource(value="chav1961.ksmgr.interfaces.importentitytype.certificate",tooltip="chav1961.ksmgr.interfaces.importentitytype.certificate.tt")
	CERTIFICATE
}
