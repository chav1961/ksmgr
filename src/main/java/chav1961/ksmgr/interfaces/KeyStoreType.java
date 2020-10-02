package chav1961.ksmgr.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.KeyStoreType/chav1961/ksmgr/i18n/i18n.xml")
public enum KeyStoreType {
	@LocaleResource(value="chav1961.ksmgr.interfaces.keystoretype.pkcs12",tooltip="chav1961.ksmgr.interfaces.keystoretype.pkcs12.tt")
	PKCS12
}
