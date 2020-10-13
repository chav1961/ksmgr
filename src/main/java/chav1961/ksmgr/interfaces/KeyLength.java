package chav1961.ksmgr.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.KeyLength/chav1961/ksmgr/i18n/i18n.xml")
public enum KeyLength {
	@LocaleResource(value="chav1961.ksmgr.interfaces.keylength.1024",tooltip="chav1961.ksmgr.interfaces.keylength.1024.tt")
	KEY1024(1024),
	@LocaleResource(value="chav1961.ksmgr.interfaces.keylength.2048",tooltip="chav1961.ksmgr.interfaces.keylength.2048.tt")
	KEY2048(256),
	@LocaleResource(value="chav1961.ksmgr.interfaces.keylength.4096",tooltip="chav1961.ksmgr.interfaces.keylength.4096.tt")
	KEY4096(1024);
	
	private final int	keyLen;
	
	KeyLength(int keyLen) {
		this.keyLen = keyLen;
	}
	
	public int getKeyLength() {
		return keyLen;
	}
}
