package chav1961.ksmgr.internal;

import java.io.File;
import java.security.KeyStore;

public class KeyStoreWrapper {
	public static final String	PASSWD_PREFIX = "keystore";
	
	public final int		entryId;
	public final KeyStore	keyStore;
	public File				file = null;

	public KeyStoreWrapper(final int entryId, final File file, final KeyStore keyStore) {
		this.entryId = entryId;
		this.file = file;
		this.keyStore = keyStore;
	} 
}