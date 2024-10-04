package chav1961.ksmgr.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreWrapper {
	public final int		entryId;
	public final KeyStore	keyStore;
	public File				file = null;

	public KeyStoreWrapper(final int entryId, final File file, final KeyStore keyStore) {
		this.entryId = entryId;
		this.file = file;
		this.keyStore = keyStore;
	} 
	
	public void changeKeyStorePassword(final char[] oldPassword, final char[] newPassword) throws KeyStoreException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			keyStore.store(baos, newPassword);
			baos.flush();
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray())) {
				keyStore.load(bais, newPassword);
			}
		} catch (IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new KeyStoreException(e);
		}
	}
}
