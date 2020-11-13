package chav1961.ksmgr.dialogs;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Enumeration;

import chav1961.ksmgr.interfaces.KeyLength;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.ksmgr.dialogs.KeyPairCreateDialog/chav1961/ksmgr/i18n/i18n.xml")
@LocaleResource(value="chav1961.ksmgr.dialogs.keypaircreatedialog",tooltip="chav1961.ksmgr.dialogs.keypaircreatedialog.tt",help="help.aboutApplication")
public class KeyPairCreateDialog implements FormManager<Object, KeyPairCreateDialog> {
	private final LoggerFacade 	facade;
	private final KeyStore 		ks;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keypaircreatedialog.keylength",tooltip="chav1961.ksmgr.dialogs.keypaircreatedialog.keylength.tt")
	@Format("30m")
	public KeyLength	keyLength = KeyLength.KEY1024;

	@LocaleResource(value="chav1961.ksmgr.dialogs.keypaircreatedialog.usesecurerandom",tooltip="chav1961.ksmgr.dialogs.keypaircreatedialog.usesecurerandom.tt")
	@Format("1m")
	public boolean		useSecureRandom = false;
	
	@LocaleResource(value="chav1961.ksmgr.dialogs.keypaircreatedialog.alias",tooltip="chav1961.ksmgr.dialogs.keypaircreatedialog.alias.tt")
	@Format("30ms")
	public String		alias = "myAlias";

	public KeyPairCreateDialog(final LoggerFacade facade, final KeyStore ks) {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (ks == null) {
			throw new NullPointerException("Keystore can't be null"); 
		}
		else {
			this.facade = facade;
			this.ks = ks;
		}
	}
	
	@Override
	public RefreshMode onField(final KeyPairCreateDialog inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		switch (fieldName) {
			case "alias"	:
				try{final Enumeration<String>	aliases = ks.aliases();
				
				while (aliases.hasMoreElements()) {
					final String	name = aliases.nextElement();
					
					if (alias.equals(name)) {
						getLogger().message(Severity.warning,"Alias ["+alias+"] already exists in the store");
						return RefreshMode.REJECT;
					}
				}
			} catch (KeyStoreException e) {
				getLogger().message(Severity.error,e,"Error processing key store: "+e.getLocalizedMessage());
				return RefreshMode.REJECT;
			}
		}
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return facade;
	}
	
	public KeyPair generate() throws NoSuchAlgorithmException {
		final KeyPairGenerator	keyGen = KeyPairGenerator.getInstance("RSA");
		
		if (useSecureRandom) {
			keyGen.initialize(keyLength.getKeyLength(),new SecureRandom());
		}
		else {
			keyGen.initialize(keyLength.getKeyLength());
		}
       return keyGen.generateKeyPair();
	}
}
