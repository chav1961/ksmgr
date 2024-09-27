package chav1961.ksmgr.interfaces;

import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.interfaces.CipherKeyLength/chav1961/ksmgr/i18n/i18n.xml")
public enum CipherKeyLength implements ModuleAccessor {
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.128",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.128.tt")
	KEY128(128),
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.256",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.256.tt")
	KEY256(256),
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.512",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.512.tt")
	KEY512(512),
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.1024",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.1024.tt")
	KEY1024(1024),
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.2048",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.2048.tt")
	KEY2048(256),
	@LocaleResource(value="chav1961.ksmgr.interfaces.cipherkeylength.4096",tooltip="chav1961.ksmgr.interfaces.cipherkeylength.4096.tt")
	KEY4096(1024);
	
	private final int	keyLen;
	
	CipherKeyLength(int keyLen) {
		this.keyLen = keyLen;
	}
	
	public int getKeyLength() {
		return keyLen;
	}
	
	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}
